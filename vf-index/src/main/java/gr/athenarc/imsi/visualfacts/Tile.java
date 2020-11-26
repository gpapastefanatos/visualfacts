package gr.athenarc.imsi.visualfacts;

import gr.athenarc.imsi.visualfacts.query.Query;
import gr.athenarc.imsi.visualfacts.util.ContainmentExaminer;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Tile {

    protected Rectangle bounds;

    protected TreeNode root;
    List<CategoricalColumn> categoricalColumns;


    public Tile(Rectangle bounds) {
        this.bounds = bounds;
    }

    public abstract Tile getLeafTile(Point point);


    public TreeNode addPoint(Point point, String[] row) {
        return getOrAddCategoricalNode(row).addPoint(point);
    }

    protected TreeNode addPoint(Point point, Stack<Short> labels) {
        return getOrAddCategoricalNode(labels).addPoint(point);
    }

    public Rectangle getBounds() {
        return bounds;
    }


    public abstract List getLeafTiles();

    public abstract List<Tile> getOverlappedLeafTiles(Query query);

    public abstract void split();


    @Override
    public String toString() {
        return "{" + this.bounds.toString() + "}";
    }


    public abstract int getMaxDepth();

    public abstract int getLeafTileCount();

    private TreeNode getOrAddCategoricalNode(String[] row) {
        if (root == null) {
            root = new TreeNode((short) 0);
        }
        TreeNode node = root;
        if (categoricalColumns == null) {
            return node;
        }
        for (CategoricalColumn categoricalColumn : categoricalColumns) {
            TreeNode child = node.getOrAddChild(categoricalColumn.getValueKey(row[categoricalColumn.getIndex()]));
            node = child;
        }
        return node;
    }

    private TreeNode getOrAddCategoricalNode(Stack<Short> labels) {
        if (root == null) {
            root = new TreeNode((short) 0);
        }
        TreeNode node = root;
        for (short label : labels) {
            TreeNode child = node.getOrAddChild(label);
            node = child;
        }
        return node;
    }

/*    private TreeNode getCategoricalNode(Stack<Short> labels) {
        TreeNode node = root;
        if (node == null)
            return null;

        for (short label : labels) {
            TreeNode child = node.getChild(label);
            node = child;
            if (node == null)
                return null;
        }
        return node;
    }*/

    public List<QueryNode> getQueryNodes(Query query, ContainmentExaminer containmentExaminer, Schema schema) {
        //we keep the old list of attrs in case node nodes match the query in this tile so that we dont expand trees unnecessarily
        List<CategoricalColumn> oldCatAttrs = categoricalColumns;

        categoricalColumns = categoricalColumns == null ? new ArrayList<>() : new ArrayList<>(categoricalColumns);
        Set<Integer> treeAttrIndexes = categoricalColumns.stream().map(CategoricalColumn::getIndex).collect(Collectors.toSet());

        //we check if the tree's categorical attributes lack any of the attrs included in the query
        Set<Integer> queryAttrs = new HashSet<>(query.getCategoricalFilters().keySet());
        if (query.getGroupByCol() != null) {
            queryAttrs.add(query.getGroupByCol());
        }
        List<CategoricalColumn> unknownQueryAttrs = queryAttrs.stream().filter(attr -> !treeAttrIndexes.contains(attr))
                .map(attrIndex -> schema.getCategoricalColumn(attrIndex))
                .sorted(Comparator.comparingInt(CategoricalColumn::getCardinality)).collect(Collectors.toList());

        categoricalColumns.addAll(unknownQueryAttrs);


        List<QueryNode> queryNodes = new ArrayList<>();
        if (root != null) {
            List<Short> pattern = categoricalColumns.stream().map(categoricalColumn -> {
                if (categoricalColumn.getIndex() == query.getGroupByCol()) {
                    return (short) -1;
                }
                String filterValue = query.getCategoricalFilters().get(categoricalColumn.getIndex());
                return filterValue == null ? null : categoricalColumn.getValueKey(filterValue);
            }).collect(Collectors.toList());
            queryNodes = getQueryNodesRec(queryNodes, containmentExaminer, root, pattern, 0, null);
        }

        if (queryNodes.isEmpty()) {
            categoricalColumns = oldCatAttrs;
        }
        return queryNodes;
    }


    private List<QueryNode> getQueryNodesRec(List<QueryNode> list, ContainmentExaminer containmentExaminer, TreeNode node, List<Short> pattern, int level, Short groupByValue) {
        // we are at a leaf node
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            list.add(new QueryNode(node, this, containmentExaminer, groupByValue, getUnknownAttrs(level)));
            return list;
        }

        Short label = pattern.get(level);

        // no filter set for current categorical
        if (label == null) {
            for (TreeNode child : node.getChildren()) {
                getQueryNodesRec(list, containmentExaminer, child, pattern, level + 1, groupByValue);
            }
        } else if (label == -1) {
            for (TreeNode child : node.getChildren()) {
                getQueryNodesRec(list, containmentExaminer, child, pattern, level + 1, child.getLabel());
            }
        } else {
            TreeNode child = node.getChild(label);
            if (child != null) {
                getQueryNodesRec(list, containmentExaminer, child, pattern, level + 1, groupByValue);
            }
        }
        return list;
    }

    private List<CategoricalColumn> getUnknownAttrs(int level) {
        return new ArrayList<>(categoricalColumns.subList(level, categoricalColumns.size()));
    }

    protected void reAddPoints(TreeNode node, Stack<Short> labels) {
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                labels.push(child.getLabel());
                reAddPoints(child, labels);
                labels.pop();
            }
        } else {
            for (Point point : node.getPoints()) {
                this.addPoint(point, labels);
            }
        }
    }

    public List<CategoricalColumn> getCategoricalColumns() {
        return categoricalColumns;
    }

    public void setCategoricalColumns(List<CategoricalColumn> categoricalColumns) {
        this.categoricalColumns = categoricalColumns;
    }
}