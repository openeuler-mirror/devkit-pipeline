public class BooleanExpressionComplexityCase
{
    public static void main(String ... args)
    {
        boolean a = true;
        boolean b = false;

        boolean c = (a & b) | (b ^ a); // OK, 1(&) + 1(|) + 1(^) = 3 (max allowed 5)

        boolean d = (a & b) | (b ^ a) | (a ^ b);
        // OK above, 1(&) + 1(|) + 1(^) + 1(|) + 1(^) = 5

        boolean e = a ^ (a || b) ^ (b || a) & (a | b);
        // violation above, 'Boolean expression complexity is 6 (max allowed is 5)'
        // 1(^) + 1(||) + 1(^) + 1(||) + 1(&) + 1(|) = 6
    }
}