package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.stack.Stack;

import java.util.function.BiFunction;
import java.util.function.Function;

import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;


// See https://en.bitcoin.it/wiki/Script

class StackOpFunctions extends AbstractOpFunctions {

    static final BiFunction<Stack, Stack, Boolean> opToAltStack = moveTopElementToOtherStack;

    static final BiFunction<Stack, Stack, Boolean> opFromAltStack = (s, a) -> moveTopElementToOtherStack.apply(a, s);

    // If the top stack value is not 0, duplicate it
    // def op_ifdup(stack):
    //    if len(stack) < 1:
    //        return False
    //    if decode_num(stack[-1]) != 0:
    //        stack.append(stack[-1])
    //    return True
    static final Function<Stack, Boolean> opIfDup = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            byte[] e = s.peek();
            int n = decodeElement(s.peek());
            if (n != 0) {
                s.push(e); // duplicate
                return true;
            }
        }
        return false;
    };

    // Puts the number of stack items onto the stack
    // def op_depth(stack):
    //    stack.append(encode_num(len(stack)))
    //    return True
    static final Function<Stack, Boolean> opDepth = (s) -> {
        s.push(encodeNumber(s.size()));
        return true;
    };

    // Removes the top stack item
    // def op_drop(stack):
    //    if len(stack) < 1:
    //        return False
    //    stack.pop()
    //    return True
    static final Function<Stack, Boolean> opDrop = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            s.pop();
            return true;
        }
        return false;
    };

    // Duplicates the top stack item
    // def op_dup(stack):
    //    if len(stack) < 1:  # <1>
    //        return False
    //    stack.append(stack[-1])  # <2>
    //    return True
    static final Function<Stack, Boolean> opDup = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            s.push(s.peek());
            return true;
        }
        return false;
    };

    // Removes the second-to-top stack item
    // def op_nip(stack):
    //    if len(stack) < 2:
    //        return False
    //    stack[-2:] = stack[-1:]
    //    return True
    // Before:  x1 x2 	After:  x2
    static final Function<Stack, Boolean> opNip = (s) -> {
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] top = s.pop(); // save 1st element
            s.pop(); // pop 2nd element
            s.push(top);
            return true;
        }
        return false;
    };

    // Copies the second-to-top stack item to the top
    // def op_over(stack):
    //    if len(stack) < 2:
    //        return False
    //    stack.append(stack[-2])
    //    return True
    static final Function<Stack, Boolean> opOver = (s) -> {
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] first = s.pop();   // save 1st element
            byte[] second = s.peek(); // get copy of 2nd element
            s.push(first);  // put top element back
            s.push(second); // put copy of 2nd element on top of old top element
            return true;
        }
        return false;
    };

    // The item n back in the stack is copied (or moved?) to the top
    // def op_pick(stack):
    //    if len(stack) < 1:
    //        return False
    //    n = decode_num(stack.pop())  // this removes top element, to be replaced by stack[n]
    //    if len(stack) < n + 1:
    //        return False
    //    stack.append(stack[-n - 1])
    //    return True
    // Before:  xn ... x2 x1 x0 <n>	   After:  xn ... x2 x1 x0 xn
    static final Function<Stack, Boolean> opPick = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            int n = decodeElement(s.pop());
            if (s.size() < n + 1) {
                return false;
            }
            byte[][] elements = popNElementsIntoArray.apply(s, n);
            byte[] newTop = elements[0];    // save the element that was stack[n]
            pushElementsOntoStack.apply(s, elements, 0, elements.length); // restore all, including the new top
            s.push(newTop); // push copy of nth element to top
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opRoll = (s) -> {
        // The item n back in the stack is moved to the top.
        // def op_roll(stack):
        //    if len(stack) < 1:
        //        return False
        //    n = decode_num(stack.pop())
        //    if len(stack) < n + 1:
        //        return False
        //    if n == 0:
        //        return True
        //    stack.append(stack.pop(-n - 1))
        //    return True
        // Before:  xn ... x2 x1 x0 <n>  After:	... x2 x1 x0 xn
        if (stackIsNotEmpty.apply(s)) {
            int n = decodeElement(s.pop());
            if (s.size() < n + 1) {
                return false;
            }
            if (n == 0) {
                return true;
            }
            byte[][] elements = popNElementsIntoArray.apply(s, n - 1);  // n-1, already popped top element to get 'n'
            byte[] newTop = elements[0];    // save the element that was stack[n]
            pushElementsOntoStack.apply(s, elements, 1, n - 1);  // restore all but new top
            s.push(newTop);
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opRot = (s) -> {
        // Before Rotate(10): [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
        // After  Rotate(10): [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 0, 1, 2, 3, 4]
        // 10 elements popped then appended to the bottom of the stack
        //
        // The top three items on the stack are rotated to the left.  (See collections rotate example above.)
        // def op_rot(stack):
        //    if len(stack) < 3:
        //        return False
        //    stack.append(stack.pop(-3))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 3) {
            s.putLast(s.pop());
            s.putLast(s.pop());
            s.putLast(s.pop());
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opSwap = (s) -> {
        // The top two items on the stack are swapped
        // Before:  x1 x2 	    After:  x2 x1
        // The bitcoin doc says it's a swap, not a rotate
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            s.push(e1);
            s.push(e2);
            return true;
        }
        // Jimmy Song implemented rotate(2), as I have implemented in unreachable code below.
        // def op_swap(stack):
        //    if len(stack) < 2:
        //        return False
        //    stack.append(stack.pop(-2))
        //    return True
        //noinspection PointlessBooleanExpression
        if (false && stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            s.putLast(e1);
            s.putLast(e2);
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opTuck = (s) -> {
        // The item at the top of the stack is copied and inserted before the second-to-top item
        // def op_tuck(stack):
        //    if len(stack) < 2:
        //        return False
        //    stack.insert(-2, stack[-1])
        //    return True
        // Before:  x1 x2       After:  x2 x1 x2
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] top = s.pop();
            byte[] secondToTop = s.pop();
            s.push(top);
            s.push(secondToTop);
            s.push(top);
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op2Drop = (s) -> {
        // Removes the top two stack items.
        // def op_2drop(stack):
        //    if len(stack) < 2:
        //        return False
        //    stack.pop()
        //    stack.pop()
        //    return True
        // Before:  x1 x2 	After:  Nothing
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            s.pop();
            s.pop();
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op2Dup = (s) -> {
        // Duplicates the top two stack items.
        // def op_2dup(stack):
        //    if len(stack) < 2:
        //        return False
        //    stack.extend(stack[-2:])
        //    return True
        // Before: x1 x2	After:  x1 x2 x1 x2
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            s.push(e2);
            s.push(e1);
            s.push(e2);
            s.push(e1);
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op3Dup = (s) -> {
        // Duplicates the top three stack items
        // def op_3dup(stack):
        //    if len(stack) < 3:
        //        return False
        //    stack.extend(stack[-3:])
        //    return True
        // Before: x1 x2 x3	    After:  x1 x2 x3 x1 x2 x3
        if (stackIsNotEmpty.apply(s) && s.size() >= 3) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            byte[] e3 = s.pop();
            s.push(e3);
            s.push(e2);
            s.push(e1);
            s.push(e3);
            s.push(e2);
            s.push(e1);
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op2Over = (s) -> {
        // Copies the pair of items two spaces back in the stack to the front
        // def op_2over(stack):
        //    if len(stack) < 4:
        //        return False
        //    stack.extend(stack[-4:-2])
        //    return True
        // Before:  (tail) x1 x2 x3 x4 (top)    After:  (tail) x1 x2 x3 x4 x1 x2 (top)
        if (stackIsNotEmpty.apply(s) && s.size() >= 4) {
            byte[] first = s.pop();
            byte[] second = s.pop();
            byte[] third = s.pop();
            byte[] fourth = s.pop();
            s.push(fourth);
            s.push(third);
            s.push(second);
            s.push(first);
            s.push(fourth);
            s.push(third);
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op2Rot = (s) -> {
        // The fifth and sixth items back are moved to the top of the stack
        // def op_2rot(stack):
        //    if len(stack) < 6:
        //        return False
        //    stack.extend(stack[-6:-4])
        //    return True
        //  Before:  x1 x2 x3 x4 x5 x6	After:  x3 x4 x5 x6 x1 x2
        //
        //  SEE https://github.com/jimmysong/programmingbitcoin/issues/139
        //  "op_2rot, according to the bitcoin script wiki, is supposed to move
        //  the 5th and 6th items to the top of the stack, but you've done a copy."
        //  (I'm doing a move)
        if (stackIsNotEmpty.apply(s) && s.size() >= 6) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            byte[] e3 = s.pop();
            byte[] e4 = s.pop();
            byte[] e5 = s.pop();
            byte[] e6 = s.pop();
            s.push(e4);
            s.push(e3);
            s.push(e2);
            s.push(e1);
            s.push(e6);
            s.push(e5);
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op2Swap = (s) -> {
        // Swaps the top two pairs of items
        // def op_2swap(stack):
        //    if len(stack) < 4:
        //        return False
        //    stack[-4:] = stack[-2:] + stack[-4:-2]
        //    return True
        // Before:  x1 x2 x3 x4	  After:  x3 x4 x1 x2
        if (stackIsNotEmpty.apply(s) && s.size() >= 4) {
            byte[] e1 = s.pop();
            byte[] e2 = s.pop();
            byte[] e3 = s.pop();
            byte[] e4 = s.pop();
            s.push(e2);
            s.push(e1);
            s.push(e4);
            s.push(e3);
            return true;
        }
        return false;
    };
}
