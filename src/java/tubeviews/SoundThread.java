package tubeviews;

import java.util.Random;

import bellmodel.MatPoint;
import bellmodel.ModelCalc;
import bellmodel.ModelData;
import firststep.internal.portaudio.BlockingStream;
import firststep.internal.portaudio.PortAudio;
import firststep.internal.portaudio.PortAudioException;
import firststep.internal.portaudio.StreamParameters;

class SoundThread extends Thread {
		private volatile boolean stopFlag = false;
		
		public void stopSound() { stopFlag = true; }
		
		private MatPoint mpt11, mpt12, mpt21, mpt22;
		
		private ModelCalc initBells(double dt, double relaxTime) {
			MatPoint wlL  = new MatPoint(1e+24,   -1.0,   0,    0,  -0.98,     0, 0, 0, 0, 0, false, true);

			mpt11 = new MatPoint(1.0,  -0.5,        -0.5,  0,  -0.5,  -0.5, 0, 0, 0, 0, true, false); 
			mpt12 = new MatPoint(1.000,  -0.5,         0.5,  0,  -0.5,   0.5, 0, 0, 0, 0, true, false); 
			mpt21 = new MatPoint(1.0000,   0.5,        -0.5,  0,   0.5,  -0.5, 0, 0, 0, 0, true, false); 
			mpt22 = new MatPoint(1.0,   0.5,         0.5,  0,   0.5,   0.5, 0, 0, 0, 0, true, false); 
			
			MatPoint wlR  = new MatPoint(1e+24,    1.0,   0,    0,   0.98,     0, 0, 0, 0, 0, false, true);
			
			MatPoint.connect(wlL, mpt11);
			MatPoint.connect(wlL, mpt12);
			MatPoint.connect(mpt11, mpt12);
			MatPoint.connect(mpt11, mpt21);
			MatPoint.connect(mpt12, mpt22);
			MatPoint.connect(mpt21, mpt22);
			MatPoint.connect(mpt21, wlR);
			MatPoint.connect(mpt22, wlR);
			
			ModelData md = new ModelData(2.e+7, 3);
			md.add(wlL);
			md.add(mpt11);
			md.add(mpt12);
			md.add(mpt21);
			md.add(mpt22);
			md.add(wlR);
			
			ModelCalc mc = new ModelCalc(md, 1, 1, dt);
			
			return mc;
		}
		
		Random r = new Random();
		
		volatile boolean kickFlag = true;
		
		public void randomKick() {
			kickFlag = true;
		}
		
		private void doKick() {
			/*for (int mptIndex = 0; mptIndex < 4; mptIndex ++)*/ {
				MatPoint[] mps = new MatPoint[] { mpt11, mpt12, mpt21, mpt22 };
				double kickValue = r.nextDouble() * 0.2 + 1;
				
				double angle;
				//if (r.nextDouble() < 0.5) {
					angle = Math.PI * (0.5 + 0.025 * r.nextDouble());
				/*} else {
					angle = -Math.PI * (0.5 + 0.025 * r.nextDouble());
				}*/
				
				int mptIndex = r.nextInt(4);
				
				mps[mptIndex].setVx(mps[mptIndex].getVx() + kickValue * Math.cos(angle));
				mps[mptIndex].setVy(mps[mptIndex].getVy() + kickValue * Math.sin(angle));
			}
			kickFlag = false;
		}
		
		public void run() {
			try {
				PortAudio.initialize();
				
				int channels = 2;
				int freq = 44100;
				int frames = (int)(freq * 0.05);	// Buffer is 0.05 second (to support slow devices)
				
				ModelCalc mc = initBells(1.0 / freq, 1.0);	// 1 second of relaxation
	
				StreamParameters isp = new StreamParameters();
				isp.channelCount = channels;
				
				float[] buf = new float[frames * channels];
				while (!stopFlag) {
					try {
						isp.device = PortAudio.getDefaultOutputDevice();
						isp.sampleFormat = PortAudio.FORMAT_FLOAT_32;
						
						BlockingStream bs = PortAudio.openStream(null, isp, freq, frames, 0);
						bs.start();
						while (true) {
							for (int i = 0; i < buf.length; i += 2) {

								double xavg = mc.doStep();
			
								buf[i] = (float) xavg;
								buf[i + 1] = (float) xavg;
							}
							if (stopFlag) break;
							bs.write(buf, frames);
							if (kickFlag) doKick();
						}
						bs.stop();
					} catch (PortAudioException e) {
						System.out.println("Audio device problem:");
						e.printStackTrace();
					} 
				}
			} catch (PortAudioException e) {
				System.out.println("Audio device problem:");
				e.printStackTrace();
			}
			System.out.println("Sound thread finished gracefully");

		}
	};
