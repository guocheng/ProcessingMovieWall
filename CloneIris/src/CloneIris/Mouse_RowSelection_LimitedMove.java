package CloneIris;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import processing.video.*;
import processing.core.*;

import java.util.*;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

@SuppressWarnings("serial")
public class Mouse_RowSelection_LimitedMove extends PApplet implements NativeKeyListener
{
	private Poster[] posters;
	private Row[] rows;
	private int SCREEN_WIDTH, SCREEN_HEIGHT;
	
	//Poster Size
	public static final int POSTER_WIDTH = 210, POSTER_HEIGHT = 320;
	
	//UI Layout
	public static final int TOP_MARGIN = 64;
	public static final int LEFT_MARGIN = 128;
	
	public static final int HORIZONTAL_GAP = 32;
	public static final int VERTICAL_GAP = 70;
	
	public static final int NUM_OF_POSTERS = 60;
	public static final int ROW = 5, COL = 12;
	
	public static final float startPosX = -1 * (COL * POSTER_WIDTH + (COL - 1) * HORIZONTAL_GAP) / 2.0f;
	public static final float startPosY = -1 * (ROW * POSTER_HEIGHT + (ROW - 1) * VERTICAL_GAP) / 2.0f;
	
	//Animation Parameters
	private static final  int MAX_ROTATION_ANGLE_Y = 5;
	private static final  int MAX_ROTATION_ANGLE_X = 3;
	
	private float rotationAngle_Y = 0, rotationAngle_X = 0;
	private float distX = 0, stepX =5f, 
			rotationStepY = 0.2f, rotateBackstepY = 0.2f, depthstepX = 10;
	
	private float distY = 0, stepY = 5f, 
			rotationStepX = 0.2f, rotateBackstepX = 0.2f;
	
	
	private float depth = 350;

	private int current_selected_index = -1;
	private int current_selected_row = 2;
	private List<MouseEventListener> mouseEventList = new ArrayList<MouseEventListener>();
	private PGraphics buffer;
	
	private boolean fullScreen = false;
	
	private float centerX, centerY;
	private float percentageX = 0f, percentageY = 0f;
	
	private boolean isScrollingY = false, isScrollingX = false;
	private float vertical_scroll_target = -0.1f, horizontal_scroll_target = -0.1f;
	
	private static final float VERTICAL_SCROLL_STEP = 6;
	private static final float FIXED_VERTICAL_SCROLL_DISTANCE = POSTER_HEIGHT + VERTICAL_GAP;
	private static final float VERTICAL_SCROLL_BOUNDARY = ROW / 2 * (FIXED_VERTICAL_SCROLL_DISTANCE);
	private static final float VERTICAL_SCROLL_TRIGGER_PERCENTAGE = 0.6f;
	
	private static final float HORIZONTAL_SCROLL_STEP = 10;
	private static final float FIXED_HORIZONTAL_SCROLL_DISTANCE = POSTER_WIDTH + HORIZONTAL_GAP;
	private static final float HORIZONTAL_SCROLL_BOUNDARY = COL / 4 * FIXED_HORIZONTAL_SCROLL_DISTANCE + FIXED_HORIZONTAL_SCROLL_DISTANCE;
	
	private static final float HORIZONTAL_SCROLL_TRIGGER_PERCENTAGE = 0.8f;
	private ScrollDirection dir = ScrollDirection.NONE;
	
	
	private PImage bg_texture = loadImage("poster/bg.png");
	private float halfWidth, halfHeight;
	private boolean isPlayingTrailer = false;
	
	private static final int MOVIE_WIDTH = 848;
	private static final int MOVIE_HEIGHT = 360;
	
	private static final int HALF_MOVIE_WIDTH = MOVIE_WIDTH / 2;
	private static final int HALF_MOVIE_HEIGHT = MOVIE_HEIGHT / 2;
	private boolean disableMenu = false;
	private Robot robot;
	
	private static final int TRIGGER_VR_TIME_MS = 500;
	private boolean isRecognizing = false;
	private int prevIndex = -1;
	
	private boolean finishedDrawing = false;
	private float bg_color = 0;
	
	public enum ScrollDirection
	{
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}
	
	public enum DataType
	{
		ROW_INDEX,
		POSTER_INDEX
	}
	
	public void setup()
	{
		if(fullScreen)
		{
			SCREEN_WIDTH = displayWidth;
			SCREEN_HEIGHT = displayHeight;
		}
		else
		{
			SCREEN_WIDTH = 800;
			SCREEN_HEIGHT = 600;
		}
		
		halfWidth = width / 2;
		halfHeight = height / 2;
		
		size(SCREEN_WIDTH, SCREEN_HEIGHT, P3D);
		buffer = createGraphics(width, height, P3D);
		//frameRate(60);
		addMouseWheelListener(new MouseWheelListener() { 
		    public void mouseWheelMoved(MouseWheelEvent mwe) { 
		      mouseWheel(mwe.getWheelRotation());
		  }}); 
		
		
		
		centerX = width/2;
		centerY = height/2;
		
		posters = new Poster[NUM_OF_POSTERS];
		rows = new Row[ROW];
		
		PImage texture = null;
		Movie movie = null;
		
		int x = 0;
		int y = 0;
		
		int index = 0;
		
		int rowWidth = COL * POSTER_WIDTH + (COL - 1) * HORIZONTAL_GAP;
		int rowHeight = POSTER_HEIGHT;
		
		for(int i = 0; i < ROW; i++)
		{
			rows[i] = new Row(i, startPosX, (startPosY + (POSTER_HEIGHT + VERTICAL_GAP) * i), rowWidth, rowHeight);
			
			for(int j = 0; j < COL; j++)
			{
				texture  = loadImage("poster/s"+(index+1)%9+".jpg");
				x = (POSTER_WIDTH + HORIZONTAL_GAP) * j;
				y = 0;
				
				movie = new Movie(this, dataPath("movie/"+(index+1)%9+".mov"));
				posters[index] = new Poster(index, i, j, x, y, POSTER_WIDTH, POSTER_HEIGHT, texture, movie);
				//List # 0 - 59 = posters
				mouseEventList.add(posters[index]);
				((Row)rows[i]).add(posters[index]);
				index++;
			}   
		}
		
		//List # 60 - 64 = rows
		for(Row r: rows)
		{
			mouseEventList.add(r);
		}
		
		mouseX = width / 2;
		mouseY = height / 2;
		
		println("X: " + startPosX + " Y: " + startPosY);
		rows[current_selected_row].onMouseEnter();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

	        public void run() {
	           stop();
	        }
	    }));
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void draw()
	{
		background(0);
		
		if(mouseX > centerX)
		{
			percentageX = (mouseX - centerX) / centerX;
		}
		else if(mouseX < centerX)
		{
			percentageX = (1 - mouseX / centerX) * -1;	// -1 for direction
		}
		else
		{
			percentageX = 0f;
		}
		
		if(mouseY > centerY)
		{
			percentageY = (mouseY - centerY) / centerY * -1;	// -1 for direction
		}
		else if(mouseY < centerY)
		{
			percentageY = (1 - mouseY / centerY);
		}
		else
		{
			percentageY = 0f;
		}
		
		
		translate(width/2, height/2, depth);
		
		pushMatrix();
		
		if(!isPlayingTrailer)
		{
			
				if(percentageX >= HORIZONTAL_SCROLL_TRIGGER_PERCENTAGE)
				{
					if(!isScrollingX && !isScrollingY)
					{
						if(distX - FIXED_HORIZONTAL_SCROLL_DISTANCE >= -HORIZONTAL_SCROLL_BOUNDARY)
						{
							horizontal_scroll_target = distX - FIXED_HORIZONTAL_SCROLL_DISTANCE;
							 dir = ScrollDirection.LEFT;
							isScrollingX = true;
						}
					}
					
					if(isScrollingX && !isScrollingY && horizontal_scroll_target != -0.1f)
					{
						doScroll();
					}
				}
				else if(percentageX <= -HORIZONTAL_SCROLL_TRIGGER_PERCENTAGE)
				{
					if(!isScrollingX && !isScrollingY)
					{
						if(distX + FIXED_HORIZONTAL_SCROLL_DISTANCE <= HORIZONTAL_SCROLL_BOUNDARY)
						{
							horizontal_scroll_target = distX + FIXED_HORIZONTAL_SCROLL_DISTANCE;
							dir = ScrollDirection.RIGHT;
							
							isScrollingX = true;
						}
					}
					
					
					if(isScrollingX && !isScrollingY && horizontal_scroll_target != -0.1)
					{
						doScroll();
					}
				}
				else
				{
					if(isScrollingX && !isScrollingY && horizontal_scroll_target != -0.1)
					{
						doScroll();
					}
				}
				
				if(distX > HORIZONTAL_SCROLL_BOUNDARY) distX = HORIZONTAL_SCROLL_BOUNDARY;
				if(distX < - HORIZONTAL_SCROLL_BOUNDARY) distX = -HORIZONTAL_SCROLL_BOUNDARY;
			
	
				if(percentageY >= VERTICAL_SCROLL_TRIGGER_PERCENTAGE)
				{
					if(!isScrollingY && !isScrollingX)
					{
						if(distY + FIXED_VERTICAL_SCROLL_DISTANCE <= VERTICAL_SCROLL_BOUNDARY)
						{
							vertical_scroll_target = distY + FIXED_VERTICAL_SCROLL_DISTANCE;
							dir = ScrollDirection.UP;
		
							rows[current_selected_row].onMouseLeave();
							if(isValid(current_selected_row-1,DataType.ROW_INDEX))
							{
								current_selected_row-=1;
								rows[current_selected_row].onMouseEnter();
								isScrollingY = true;
								distX = rows[current_selected_row].getDistX();
							}
						}
					}
					
					if(isScrollingY && !isScrollingX && vertical_scroll_target != -0.1)
					{
						doScroll();
					}
				}
				else if(percentageY <= -VERTICAL_SCROLL_TRIGGER_PERCENTAGE)
				{
					if(!isScrollingY && !isScrollingX)
					{
						if(distY - FIXED_VERTICAL_SCROLL_DISTANCE >= -VERTICAL_SCROLL_BOUNDARY)
						{
							vertical_scroll_target = distY - FIXED_VERTICAL_SCROLL_DISTANCE;
							dir = ScrollDirection.DOWN;
		
							rows[current_selected_row].onMouseLeave();
							current_selected_row+=1;
							rows[current_selected_row].onMouseEnter();
							isScrollingY = true;
							distX = rows[current_selected_row].getDistX();
						}
					}
					
					if(isScrollingY && !isScrollingX &&  vertical_scroll_target != -0.1)
					{
						doScroll();
					}
				}
				else
				{
					if(isScrollingY && !isScrollingX && vertical_scroll_target != -0.1)
					{
						doScroll();
					}
				}
				
				if(distY > VERTICAL_SCROLL_BOUNDARY) distY = VERTICAL_SCROLL_BOUNDARY; 
				if(distY < -VERTICAL_SCROLL_BOUNDARY) distY = -VERTICAL_SCROLL_BOUNDARY;
			
			

				testHover();
			
				if(isValid(current_selected_row, DataType.ROW_INDEX))
				{
					rows[current_selected_row].setDistX(distX);
				}
				
				translate(0,distY);
			}
//			else
//			{
////				if(vertical_scroll_target != -0.1 || horizontal_scroll_target != -0.1)
////				{
////					if(dir == ScrollDirection.UP)
////					{
////						distY += VERTICAL_SCROLL_STEP;
////						
////						if(distY >= vertical_scroll_target)
////						{
////							distY = vertical_scroll_target;
////							isScrollingY = false;
////							vertical_scroll_target = -0.1f;
////						}
////					}
////
////					if(isValid(current_selected_row, DataType.ROW_INDEX))
////					{
////						rows[current_selected_row].setDistX(distX);
////					}
////					
////					
////					translate(0,distY);
////				}
//			}
			
			
			for(Row r : rows)
			{
				r.draw(this.g, Poster.RenderMode.TEXTURE);
			}
			
			if(!isRecognizing)
			{
				if(isValid(current_selected_index, DataType.POSTER_INDEX))
				{
					if(posters[current_selected_index].getHoverTime() >= TRIGGER_VR_TIME_MS)
					{
						this.triggerVR();
						isRecognizing = true;
						prevIndex = current_selected_index;
					}
				}
			}
			else
			{
				if(prevIndex != current_selected_index)
				{
					this.stopVR();
					isRecognizing = false;
					prevIndex = current_selected_index;
				}
			}
		
		popMatrix();
		
		pushMatrix();
			scale(0.63f);
			drawOverlay(this.g, bg_texture);
		popMatrix();
		
		if(!isScrollingX && !isScrollingY)
		{
			if(isPlayingTrailer) drawLightBox(this.g, current_selected_index);
		}
	}
	
	private void drawLightBox(PGraphics g, int id)
	{
		pushMatrix();
			g.fill(0,0,0,200);
			g.rect(-halfWidth,-halfHeight, width, height);
			// White outline
			g.strokeWeight(10);
			g.stroke(255);
			
			// Box Background
			g.fill(0);
			g.rect(-HALF_MOVIE_WIDTH, -HALF_MOVIE_HEIGHT, MOVIE_WIDTH, MOVIE_HEIGHT, 5);
			
			g.noFill();
			g.noStroke();
			// Trailer
	
			image(posters[current_selected_index].getMovie(),-HALF_MOVIE_WIDTH,-HALF_MOVIE_HEIGHT);

		popMatrix();
	}

	private void doScroll()
	{
		if(dir == ScrollDirection.UP)
		{
			distY += VERTICAL_SCROLL_STEP;
			
			if(distY >= vertical_scroll_target)
			{
				distY = vertical_scroll_target;
				isScrollingY = false;
				vertical_scroll_target = -0.1f;
			}
		}
		else if(dir == ScrollDirection.DOWN)
		{
			distY -= VERTICAL_SCROLL_STEP;
			
			if(distY <= vertical_scroll_target)
			{
				distY = vertical_scroll_target;
				isScrollingY = false;
				vertical_scroll_target = -0.1f;
			}
		}
		else if(dir == ScrollDirection.RIGHT)
		{
			distX += HORIZONTAL_SCROLL_STEP;
			
			if(distX >= horizontal_scroll_target)
			{
				distX = horizontal_scroll_target;
				isScrollingX = false;
				horizontal_scroll_target = -0.1f;
			}
		}
		else if(dir == ScrollDirection.LEFT)
		{
			distX -= HORIZONTAL_SCROLL_STEP;
			
			if(distX <= horizontal_scroll_target)
			{
				distX = horizontal_scroll_target;
				isScrollingX = false;
				horizontal_scroll_target = -0.1f;
			}
			
			
		}
	}
	
	private void testHover()
	{
		buffer.beginDraw();
			buffer.background(0);
			
			buffer.translate(width/2, height/2, depth);
//			buffer.rotateY(radians(rotationAngle_Y));
//			buffer.rotateX(radians(rotationAngle_X));
			buffer.translate(0,distY);
			
			for(Row r : rows)
			{
				r.draw(buffer, Poster.RenderMode.FILL);
			}
		buffer.endDraw();
		
		int color = buffer.get(mouseX, mouseY) & 0xff;	// 0xff changes singed int to unsigned int, it also extract the last 8 bits from the int in this case

		int posterId = getId(color);
		
		
		if(posterId >= 0)
		{
			
			if(current_selected_index == - 1)
			{
				mouseEventList.get(posterId).onMouseEnter();
				current_selected_index = posterId;
			}
			
			if(current_selected_index!= posterId)
			{
				//This line is added because users can move the mouse
				// pointer faster than the window's refresh rate
				// for instance, on frame #1, the pointer is inside
				// one poster, one frame #2, the pointer has already
				// been moved to another poster. There is no way to detect
				// the transition faster than the window's refresh rate
				// thus, when the current_selected_index is different from
				// id, trigger the onMouseLeave() on the previous selected
				// poster first to avoid both posters (previously selected &
				// currently selected) being enlarged
				if(current_selected_index >=0)
				{
					mouseEventList.get(current_selected_index).onMouseLeave();
				}

				
				mouseEventList.get(posterId).onMouseEnter();
				current_selected_index = posterId;
			}
		}
		else // user clicked on background
		{
			if(current_selected_index != - 1)
			{
				mouseEventList.get(current_selected_index).onMouseLeave();
			}

			current_selected_index = -1;
		}
	}
	
	private void triggerVR()
	{
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_T);
		
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(KeyEvent.VK_T);
	}
	
	private void stopVR()
	{
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_Y);
		
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(KeyEvent.VK_Y);
	}
	
	public void mousePressed()
	{
		
			
		if(isPlayingTrailer)
		{
			if(isValid(current_selected_index, DataType.POSTER_INDEX))
			{
				println("stop " + current_selected_index);
				posters[current_selected_index].getMovie().stop();
				isPlayingTrailer = false;
				disableMenu = false;
				finishedDrawing = false;
				bg_color = 0;
			}
		}
		else
		{
			if(!isScrollingX && !isScrollingY)
			{
				if(isValid(current_selected_index, DataType.POSTER_INDEX))
				{
					println("play " + current_selected_index);
					posters[current_selected_index].getMovie().play();
					isPlayingTrailer = true;
					disableMenu = true;
				}
			}
		}
		
	}
	
	private boolean isValid(int index, DataType type)
	{
		if(type == DataType.POSTER_INDEX)
		{
			if(index >= 0 && index < (COL * ROW - 1))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(type == DataType.ROW_INDEX)
		{
			if(index >=0 && index <= (ROW - 1))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	private void drawOverlay(PGraphics g, PImage texture)
	{
		g.beginShape();
		
			g.textureMode(PConstants.NORMAL);
			g.texture(texture);
			
			g.vertex(-halfWidth, -halfHeight, 1,1);
			g.vertex(halfWidth, -halfHeight, 1, 0);
			g.vertex(halfWidth, halfHeight, 0,0);
			g.vertex(-halfWidth, halfHeight, 0, 1);
			
		g.endShape();
	}
	
	public void keyPressed()
	{
		if(key=='s')
		{
			if(isValid(current_selected_index, DataType.POSTER_INDEX))
			{
				posters[current_selected_index].getMovie().stop();
				isPlayingTrailer = false;
				disableMenu = false;
				isRecognizing = false;
			}	
		}
		
		if(key=='p')
		{
			if(isValid(current_selected_index, DataType.POSTER_INDEX))
			{
				posters[current_selected_index].getMovie().play();
				isPlayingTrailer = true;
				disableMenu = true;
				isRecognizing = false;
			}
		}
	}
	
	public void movieEvent(Movie m)
	{
		m.read();
	}
	
	private int getId(int color)
	{
		return color - 101;
	}
	
	public void mouseWheel(int delta)
	{
		depth -= delta*depthstepX;
	}
	
	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--present", "CloneIris.Mouse_RowSelection_LimitedMove" });
		
		try {
               GlobalScreen.registerNativeHook();
       }
       catch (NativeHookException ex) {
               System.err.println("There was a problem registering the native hook.");
               System.err.println(ex.getMessage());

               System.exit(1);
       }

       //Construct the example object and initialze native hook.
       GlobalScreen.getInstance().addNativeKeyListener(new Mouse_RowSelection_LimitedMove());
	}
	
	public void stop()
	{
		if(isPlayingTrailer)
		{
			if(isValid(current_selected_index, DataType.POSTER_INDEX))
			{	
				if(posters[current_selected_index].getMovie() != null)
				{
					posters[current_selected_index].getMovie().stop();
					isPlayingTrailer = false;
				}
			}
		}
		for(Poster p: posters)
		{
			p.setMovie(null);
		}
		
		super.stop();
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		if(e.getKeyChar() == 'p')
		{
			System.out.println("Play!");
		}
		
		if(e.getKeyChar() == 's')
		{
			System.out.println("Stop!");
		}
	}
}
