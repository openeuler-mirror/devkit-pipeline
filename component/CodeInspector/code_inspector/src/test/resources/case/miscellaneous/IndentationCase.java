public class IndentationCase {
    String field;               // basicOffset
    int[] arr = {
        67, 34, // basicOffset
            5,                      // arrayInitIndent
            6 };                    // arrayInitIndent
    void bar() throws Exception // basicOffset
    {                           // braceAdjustment
        foo();                  // basicOffset
    }                           // braceAdjustment
    void foo() {                // basicOffset
        if ((cond1 && cond2)    // basicOffset
                || (cond3 && cond4)    // lineWrappingIndentation, forceStrictCondition
                ||!(cond5 && cond6)) { // lineWrappingIndentation, forceStrictCondition
            field.doSomething()          // basicOffset
                    .doSomething()           // lineWrappingIndentation and forceStrictCondition
                    .doSomething( c -> {     // lineWrappingIndentation and forceStrictCondition
                        return c.doSome();   // basicOffset
                    });
        }
    }
    void fooCase()                // basicOffset
        throws Exception {        // throwsIndent
        switch (field) {          // basicOffset
        case "value" : bar(); // caseIndent
        }
    }
}