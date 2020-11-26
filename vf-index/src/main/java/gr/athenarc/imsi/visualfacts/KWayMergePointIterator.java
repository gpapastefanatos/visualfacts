package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;


public class KWayMergePointIterator extends AbstractPointIterator {

    private QueryNode currentQueryNode;
    private PriorityQueue<NodePointsPeekingIterator> tilesPQueue;


    public KWayMergePointIterator(List<NodePointsIterator> nodePointsIterators) {
        Comparator<NodePointsPeekingIterator> comparator = new Ordering<NodePointsPeekingIterator>() {
            @Override
            public int compare(NodePointsPeekingIterator i1, NodePointsPeekingIterator i2) {
                return Long.compare(i1.peek().getFileOffset(), i2.peek().getFileOffset());
            }
        };
        tilesPQueue = new PriorityQueue<>(nodePointsIterators.size() > 0 ? nodePointsIterators.size() : 1, comparator);
        for (NodePointsIterator nodePointsIterator : nodePointsIterators) {
            if (nodePointsIterator.hasNext()) {
                tilesPQueue.add(new NodePointsPeekingIterator(nodePointsIterator));
            }
        }
    }

    protected Point getNext() {
        try {
            NodePointsPeekingIterator nodePointsPeekingIt = tilesPQueue.remove();
            Point point = nodePointsPeekingIt.next();
            currentQueryNode = nodePointsPeekingIt.getQueryNode();

            if (nodePointsPeekingIt.hasNext()) {
                tilesPQueue.add(nodePointsPeekingIt);
            }
            return point;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public QueryNode getCurrentQueryNode() {
        return currentQueryNode;
    }
}
