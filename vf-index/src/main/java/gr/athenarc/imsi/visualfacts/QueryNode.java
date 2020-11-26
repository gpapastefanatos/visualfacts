package gr.athenarc.imsi.visualfacts;

import gr.athenarc.imsi.visualfacts.util.ContainmentExaminer;

import java.util.List;


public class QueryNode {

    private Short groupByValue;
    private TreeNode node;
    private Tile tile;
    private ContainmentExaminer containmentExaminer;
    private List<CategoricalColumn> unknownCatAttrs;


    public QueryNode(TreeNode node, Tile tile, ContainmentExaminer containmentExaminer, Short groupByValue, List<CategoricalColumn> unknownCatAttrs) {
        this.groupByValue = groupByValue;
        this.node = node;
        this.tile = tile;
        this.containmentExaminer = containmentExaminer;
        this.unknownCatAttrs = unknownCatAttrs;
    }

    public Short getGroupByValue() {
        return groupByValue;
    }

    public TreeNode getNode() {
        return node;
    }

    public Tile getTile() {
        return tile;
    }

    public ContainmentExaminer getContainmentExaminer() {
        return containmentExaminer;
    }

    public List<CategoricalColumn> getUnknownCatAttrs() {
        return unknownCatAttrs;
    }

    public boolean isFullyContained() {
        return containmentExaminer == null;
    }
}