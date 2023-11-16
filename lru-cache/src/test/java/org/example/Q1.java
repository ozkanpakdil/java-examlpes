package org.example;

import org.junit.jupiter.api.Test;

public class Q1 {

    public class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        StringBuilder r = new StringBuilder();
        int r1 = 0, r2 = 0;
        while (l1.next != null) {
            r.append(l1.val);
            l1 = l1.next;
        }
        String t=r.reverse().toString();
        System.out.println("t:"+t);
        r1 = Integer.valueOf(t);
        r = new StringBuilder();

        while (l2.next != null) {
            r.append(l2.val);
            l2 = l2.next;
        }
        r2 = Integer.parseInt(r.reverse().toString());
        r=new StringBuilder();
        Integer i = Integer.valueOf(String.valueOf(r.append(r1 + r2).reverse()));
        return new ListNode(i);
    }

    @Test
    void test1(){
        ListNode l1=new ListNode(4);
        ListNode l2=new ListNode(2,l1);
        ListNode l3=new ListNode(7,l2);

        ListNode l4=new ListNode(3);
        ListNode l5=new ListNode(2,l4);
        ListNode l6=new ListNode(1,l5);

        ListNode listNode = addTwoNumbers(l3, l6);
        System.out.println(listNode.val);

    }
}
