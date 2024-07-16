class SuperCloneCase {
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

class SuperCloneB {
    private int b;

    // violation below, "Method 'clone' should call 'super.clone'."
    public SuperCloneB clone() {
        SuperCloneB other = new SuperCloneB();
        other.b = this.b;
        return other;
    }
}

class SuperCloneC {

    public SuperCloneC clone() throws CloneNotSupportedException {
        return (SuperCloneC) super.clone();
    }
}