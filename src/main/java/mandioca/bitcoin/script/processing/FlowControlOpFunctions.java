package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.stack.Stack;

import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

// See https://en.bitcoin.it/wiki/Script

class FlowControlOpFunctions extends AbstractOpFunctions {

    static final Supplier<Boolean> opNoOp = () -> true;

    static final Function<Stack, Boolean> opIf = (s) -> {
        // TODO
        // If the top stack value is not False, the statements are executed. The top stack value is removed
        // def op_if(stack, items):
        //    if len(stack) < 1:
        //        return False
        //    # go through and re-make the items array based on the top stack element
        //    true_items = []
        //    false_items = []
        //    current_array = true_items
        //    found = False
        //    num_endifs_needed = 1
        //    while len(items) > 0:
        //        item = items.pop(0)
        //        if item in (99, 100):
        //            # nested if, we have to go another endif
        //            num_endifs_needed += 1
        //            current_array.append(item)
        //        elif num_endifs_needed == 1 and item == 103:
        //            current_array = false_items
        //        elif item == 104:
        //            if num_endifs_needed == 1:
        //                found = True
        //                break
        //            else:
        //                num_endifs_needed -= 1
        //                current_array.append(item)
        //        else:
        //            current_array.append(item)
        //    if not found:
        //        return False
        //    element = stack.pop()
        //    if decode_num(element) == 0:
        //        items[:0] = false_items
        //    else:
        //        items[:0] = true_items
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
            // return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opNotIf = (s) -> {
        // TODO
        // def op_notif(stack, items):
        //    if len(stack) < 1:
        //        return False
        //    # go through and re-make the items array based on the top stack element
        //    true_items = []
        //    false_items = []
        //    current_array = true_items
        //    found = False
        //    num_endifs_needed = 1
        //    while len(items) > 0:
        //        item = items.pop(0)
        //        if item in (99, 100):
        //            # nested if, we have to go another endif
        //            num_endifs_needed += 1
        //            current_array.append(item)
        //        elif num_endifs_needed == 1 and item == 103:
        //            current_array = false_items
        //        elif item == 104:
        //            if num_endifs_needed == 1:
        //                found = True
        //                break
        //            else:
        //                num_endifs_needed -= 1
        //                current_array.append(item)
        //        else:
        //            current_array.append(item)
        //    if not found:
        //        return False
        //    element = stack.pop()
        //    if decode_num(element) == 0:
        //        items[:0] = true_items
        //    else:
        //        items[:0] = false_items
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };


    static final Function<Stack, Boolean> opElse = (s) -> {
        // If the preceding OP_IF or OP_NOTIF or OP_ELSE was not executed then these statements are and if the
        // preceding OP_IF or OP_NOTIF or OP_ELSE was executed then these statements are not.
        //
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };

    static final Function<Stack, Boolean> opEndIf = (s) -> {
        // Ends an if/else block. All blocks must end, or the transaction is invalid.
        // An OP_ENDIF without OP_IF earlier is also invalid.
        //
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };


    static final Function<Stack, Boolean> opVerify = (s) -> {
        // Marks transaction as invalid if top stack value is not true. The top stack value is removed.
        // def op_verify(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = stack.pop()
        //    if decode_num(element) == 0:
        //        return False
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            return isTrue.apply(s.pop());
        }
        return false;
    };

    static final Supplier<Boolean> opReturn = () -> false;
}
