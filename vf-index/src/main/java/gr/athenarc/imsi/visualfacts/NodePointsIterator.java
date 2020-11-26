package gr.athenarc.imsi.visualfacts;

import gr.athenarc.imsi.visualfacts.util.ContainmentExaminer;


public class NodePointsIterator extends AbstractPointIterator {
    QueryNode queryNode;
    private int i = -1;

    public NodePointsIterator(QueryNode queryNode) {
        this.queryNode = queryNode;
    }

    protected Point getNext() {
        ContainmentExaminer containmentExaminer = queryNode.getContainmentExaminer();
        TreeNode node = queryNode.getNode();
        try {
            if (containmentExaminer == null) {
                return node.getPoints().get(++i);
            }
            Point point;
            while (!containmentExaminer.contains(point = node.getPoints().get(++i))) ;
            return point;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public QueryNode getQueryNode() {
        return queryNode;
    }
}