package firststep.demo.base;

import java.util.LinkedList;

import firststep.Canvas;
import firststep.Framebuffer;

public class AnimationsGroup extends Animation {

	private LinkedList<Animation> animators = new LinkedList<>();

	protected AnimationsGroup(Animation previous, float duration,
			Aftermath aftermath) {
		super(previous, duration, aftermath);
	}
	
	public AnimationsGroup(float startTime, float duration, Aftermath aftermath) {
		super(startTime, duration, aftermath);
	}
	
	protected void addAnimation(Animation animation) {
		animators.add(animation);
	}
	
	protected void removeAnimation(Animation animation) {
		animators.remove(animation);
	}
	
	public void clear() {
		animators.clear();
	}

	@Override
	protected void frame(Framebuffer fb, float timeSinceStart) {
		LinkedList<Animation> obsoletes = new LinkedList<Animation>();
		for (Animation anim : animators) {
			anim.setCurrentTime(timeSinceStart);
			anim.render(fb);
			if (!anim.isActual(timeSinceStart) && anim.getAftermath() == Aftermath.REMOVE) {
				obsoletes.add(anim);
			}
		}
		animators.removeAll(obsoletes);
	}
}
