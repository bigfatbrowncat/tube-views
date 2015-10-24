package firststep.demo.base;

import firststep.Framebuffer;
import firststep.contracts.Animatable;

public abstract class Animation implements Animatable {
	public enum Aftermath {
		SAVE, REMOVE
	}

	private float startTime, duration;
	private float currentTime;
	private Aftermath aftermath;
	
	public void render(Framebuffer fb) {
		if (currentTime > startTime) {
			if (currentTime < startTime + duration || aftermath == Aftermath.SAVE) {
				frame(fb, Math.min(currentTime - startTime, duration));
			}
		}
	}
	
	@Override
	public void setCurrentTime(float time) {
		this.currentTime = time;
	}
	
	public boolean isActual(float currentTime) {
		return currentTime < startTime + duration;
	}
	
	
	protected abstract void frame(Framebuffer fb, float currentTime);
	
	public Animation(float startTime, float duration, Aftermath aftermath) {
		this.startTime = startTime;
		this.duration = duration;
		this.aftermath = aftermath;
	}
	
	/**
	 * Creates one animation after another
	 * @param previous
	 * @param duration
	 * @param aftermath
	 */
	protected Animation(Animation previous, float duration, Aftermath aftermath) {
		this(previous.startTime + previous.duration, duration, aftermath);
	}
	
	public float getStartTime() {
		return startTime;
	}
	
	@Override
	public Float getDuration() {
		return duration;
	}
	
	public Aftermath getAftermath() {
		return aftermath;
	}
}
