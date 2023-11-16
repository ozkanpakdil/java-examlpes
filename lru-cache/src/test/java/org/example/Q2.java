package org.example;

import org.junit.jupiter.api.Test;

public class Q2 {
    class Node {
        public Node(int v) {
            data = v;
        }

        int data;
        Node left;
        Node right;
    }

    boolean checkBST(Node root) {
        if (root == null) return true;
        if (root.left != null && root.data < root.left.data)
            return false;
        if (root.left != null &&
                root.right != null &&
                root.left.data > root.right.data)
            return false;
        if (checkBST(root.left) && checkBST(root.right))
            return true;
        else
            return false;
    }


    @Test
    void test1() {
        Node l1 = new Node(6);
        Node l2 = new Node(5);
        Node l3 = new Node(7);
        Node l4 = new Node(3);
        Node l5 = new Node(2);
        Node l6 = new Node(1);
        l1.left = l2;
        l1.right = l3;
        l2.left = l4;
        System.out.println(checkBST(l1));

    }
}
