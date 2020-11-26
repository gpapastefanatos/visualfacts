package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.Range;
import com.google.common.math.StatsAccumulator;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import gr.athenarc.imsi.visualfacts.init.InitializationPolicy;
import gr.athenarc.imsi.visualfacts.init.InitializationResults;
import gr.athenarc.imsi.visualfacts.query.Query;
import gr.athenarc.imsi.visualfacts.query.QueryResults;
import gr.athenarc.imsi.visualfacts.util.ContainmentExaminer;
import gr.athenarc.imsi.visualfacts.util.XContainmentExaminer;
import gr.athenarc.imsi.visualfacts.util.XYContainmentExaminer;
import gr.athenarc.imsi.visualfacts.util.YContainmentExaminer;
import gr.athenarc.imsi.visualfacts.util.io.RandomAccessReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gr.athenarc.imsi.visualfacts.config.IndexConfig.*;

public class Veti {

    private static final Logger LOG = LogManager.getLogger(Veti.class);

    private boolean isInitialized = false;

    private RandomAccessReader randomAccessReader;

    private Grid grid;

    private Schema schema;

    private String initMode;

    private Integer catNodeBudget;

    private InitializationPolicy initializationPolicy;

    public Veti(Schema schema, Integer catNodeBudget, String initMode) {
        this.schema = schema;
        this.initMode = initMode;
        this.catNodeBudget = catNodeBudget;
    }

    public InitializationResults initialize(Query q0) {
        if (isInitialized)
            throw new IllegalStateException("The index is already initialized");

        Integer measureCol = null;

        if (q0 != null) {
            measureCol = q0.getMeasureCol();
            initializationPolicy = InitializationPolicy.getInitializationPolicy(initMode, q0, (int) (GRID_SIZE * GRID_SIZE * SUBTILE_RATIO), schema, catNodeBudget);
        }


        List<CategoricalColumn> categoricalColumns = schema.getCategoricalColumns();
        List<Integer> catColIndexes = categoricalColumns.stream().mapToInt(CategoricalColumn::getIndex).boxed().collect(Collectors.toList());
        List<Integer> colIndexes = new ArrayList<>();

        colIndexes.add(schema.getxColumn());
        colIndexes.add(schema.getyColumn());
        colIndexes.addAll(catColIndexes);
        if (measureCol != null) {
            colIndexes.add(measureCol);
        }

        grid = new Grid(initializationPolicy, schema.getBounds(), schema.getCategoricalColumns(), GRID_SIZE);
        grid.split();
        double totalUtil = 0d;
        if (initializationPolicy != null) {
            totalUtil = initializationPolicy.initTileTreeCategoricalAttrs(grid.getLeafTiles());
        }

        CsvParserSettings parserSettings = schema.createCsvParserSettings();
        parserSettings.selectIndexes(colIndexes.toArray(new Integer[colIndexes.size()]));
        parserSettings.setColumnReorderingEnabled(false);
        CsvParser parser = new CsvParser(parserSettings);

        int i = 0;

        parser.beginParsing(new File(schema.getCsv()));
        String[] row;
        long rowOffset = parser.getContext().currentChar() - 1;
        while ((row = parser.parseNext()) != null) {
            i++;
            try {
                Point point = new Point(Float.parseFloat(row[schema.getxColumn()]), Float.parseFloat(row[schema.getyColumn()]), rowOffset);
                TreeNode node = this.grid.addPoint(point, row);
                if (measureCol != null) {
                    Float value = Float.parseFloat(row[measureCol]);
                    node.adjustStats(value);
                }
            } catch (Exception e) {
                LOG.error("Problem parsing row number " + i + ": " + Arrays.toString(row), e);
                continue;
            } finally {
                rowOffset = parser.getContext().currentChar() - 1;
            }
        }


        parser.stopParsing();
        isInitialized = true;


        // todo evaluate q0
        InitializationResults initResults = new InitializationResults(q0);
        initResults.setTotalUtil(totalUtil);
        return initResults;
    }


    public QueryResults executeQuery(Query query) throws IOException {
        if (!isInitialized) {
            return initialize(query);
        }
        Integer measureCol = query.getMeasureCol();
        Rectangle rect = query.getRect();

        CategoricalColumn groupByColumn = null;
        if (query.getGroupByCol() != null) {
            groupByColumn = schema.getCategoricalColumn(query.getGroupByCol());
        }

        QueryResults queryResults = new QueryResults(query);

        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(schema.getCsv()));
        }
        List<NodePointsIterator> rawIterators = new ArrayList<>();

        int fullyContainedTilesCount = 0;

        List<QueryNode> nodesToExpand = new ArrayList<>();

        List<Tile> leafTiles = this.grid.getOverlappedLeafTiles(query);


        Set<CategoricalColumn> catAttrsToRead = new HashSet<>();
        for (Tile leafTile : leafTiles) {
            ContainmentExaminer containmentExaminer = getContainmentExaminer(leafTile, rect);
            boolean isFullyContained = containmentExaminer == null;
            if (isFullyContained) {
                fullyContainedTilesCount++;
            }

            List<QueryNode> queryNodes = leafTile.getQueryNodes(query, containmentExaminer, schema);
            int count = 0;
            for (QueryNode queryNode : queryNodes) {
                TreeNode node = queryNode.getNode();
                if (!isFullyContained || !node.hasStats()) {
                    count += node.getPoints().size();
                }
            }

            if (count > THRESHOLD) {
                leafTile.split();
                queryNodes = leafTile.getOverlappedLeafTiles(query).stream()
                        .flatMap(tile -> tile.getQueryNodes(query, containmentExaminer, schema).stream()).collect(Collectors.toList());
            }

            for (QueryNode queryNode : queryNodes) {
                TreeNode node = queryNode.getNode();

                //add unknown attrs for that node to cat attrs to read. These do not include only query attrs but also missing attrs in incomplete leaves
                catAttrsToRead.addAll(queryNode.getUnknownCatAttrs());

                StatsAccumulator nodeStats = node.getStats();
                Short groupByValue = queryNode.getGroupByValue();

                boolean hasUnknownAttrs = queryNode.getUnknownCatAttrs() != null && !queryNode.getUnknownCatAttrs().isEmpty();

                if (isFullyContained && hasUnknownAttrs && !initMode.equals("valinor")) {
                    nodesToExpand.add(queryNode);
                }

                //todo unknownCatAttrs may not be empty but including only attrs missing from the node but not present in the query
                if (isFullyContained && nodeStats != null && !hasUnknownAttrs) {
                    queryResults.adjustStats(groupByColumn == null ? null : groupByColumn.getValue(groupByValue), nodeStats.snapshot());
                } else {
                    rawIterators.add(new NodePointsIterator(queryNode));
                }
            }
        }

        List<Integer> cols = new ArrayList<>(measureCol);
        if (measureCol != null) {
            cols.add(measureCol);
        }
        cols.addAll(catAttrsToRead.stream().map(CategoricalColumn::getIndex).collect(Collectors.toList()));

        CsvParserSettings parserSettings = schema.createCsvParserSettings();
        parserSettings.selectIndexes(cols.toArray(new Integer[cols.size()]));
        parserSettings.setColumnReorderingEnabled(false);
        CsvParser parser = new CsvParser(parserSettings);

        KWayMergePointIterator pointIterator = new KWayMergePointIterator(rawIterators);
        int ioCount = 0;
        while (pointIterator.hasNext()) {
            ioCount++;
            Point point = pointIterator.next();
            try {
                randomAccessReader.seek(point.getFileOffset());
                String line = randomAccessReader.readLine();
                if (line != null) {
                    String[] row = parser.parseLine(line);

                    float measureValue = Float.parseFloat(row[measureCol]);
                    QueryNode queryNode = pointIterator.getCurrentQueryNode();
                    TreeNode node = queryNode.getNode();

                    if (queryNode.isFullyContained()) {
                        //we expand the node with unknown attrs
                        if (!initMode.equals("valinor") && queryNode.getUnknownCatAttrs() != null && !queryNode.getUnknownCatAttrs().isEmpty()) {
                            for (CategoricalColumn unknownAttr : queryNode.getUnknownCatAttrs()) {
                                node = node.getOrAddChild(unknownAttr.getValueKey(row[unknownAttr.getIndex()]));
                            }
                            node.addPoint(point);
                            node.adjustStats(measureValue);
                        } else if (queryNode.getUnknownCatAttrs() == null || queryNode.getUnknownCatAttrs().isEmpty()) {
                            queryNode.getNode().adjustStats(measureValue);
                        }
                    }
                    String groupByValue = null;
                    if (query.getGroupByCol() != null) {
                        groupByValue = queryNode.getGroupByValue() != null ? groupByColumn.getValue(queryNode.getGroupByValue()) : row[query.getGroupByCol()];
                    }

                    if (checkUnknownAttrs(query, row, queryNode.getUnknownCatAttrs())) {
                        queryResults.adjustStats(groupByColumn == null ? null : groupByValue, measureValue);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading from raw file", e);
            }
        }

        for (QueryNode queryNode : nodesToExpand) {
            queryNode.getNode().convertToNonleaf();
        }

        queryResults.setTileCount(leafTiles.size());
        queryResults.setFullyContainedTileCount(fullyContainedTilesCount);
        queryResults.setIoCount(ioCount);
        queryResults.setExpandedNodeCount(nodesToExpand.size());

        return queryResults;
    }

    private boolean checkUnknownAttrs(Query query, String[] row, List<CategoricalColumn> unknownCatAttrs) {
        boolean check = true;
        for (CategoricalColumn categoricalColumn : unknownCatAttrs) {
            String filterValue = query.getCategoricalFilters().get(categoricalColumn.getIndex());
            if (filterValue != null) {
                String rowValue = row[categoricalColumn.getIndex()];
                check = check && rowValue != null && rowValue.equals(filterValue);
            }
        }
        return check;
    }

    private ContainmentExaminer getContainmentExaminer(Tile tile, Rectangle query) {

        Range<Float> queryXRange = query.getXRange();
        Range<Float> queryYRange = query.getYRange();
        boolean checkX = !queryXRange.encloses(tile.getBounds().getXRange());
        boolean checkY = !queryYRange.encloses(tile.getBounds().getYRange());

        ContainmentExaminer containmentExaminer = null;
        if (checkX && checkY) {
            containmentExaminer = new XYContainmentExaminer(queryXRange, queryYRange);
        } else if (checkX) {
            containmentExaminer = new XContainmentExaminer(queryXRange);
        } else if (checkY) {
            containmentExaminer = new YContainmentExaminer(queryYRange);
        }
        return containmentExaminer;
    }

    public int getLeafTileCount() {
        return this.grid.getLeafTileCount();
    }


    public int getMaxDepth() {
        return grid.getMaxDepth();
    }

    @Override
    public String toString() {
        return grid.printTiles();
    }
}
