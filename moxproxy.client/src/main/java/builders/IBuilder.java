package builders;


public interface IBuilder<ParentBuilder extends IBuilder> {

    ParentBuilder backToParent();
}
