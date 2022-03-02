package experiment;

import java.util.ArrayList;
import java.util.List;

public class ExpNode<T> {
    private final String NAME = "ExpNode/";

    private T data;
    private ExpNode<T> parent;
    private List<ExpNode<T>> children;

    public ExpNode(T dt) {
        data = dt;
        parent = null;
        children = new ArrayList<>();
    }

    public void addChild(ExpNode<T> child) {
        children.add(child);
    }

    public T getData() {
        return data;
    }

    public ExpNode<T> getChild(int childInd) {
        return children.get(childInd);
    }

    public List<ExpNode<T>> getChildren() {
        return children;
    }

    public int getNChildren() {
        return children.size();
    }

}
