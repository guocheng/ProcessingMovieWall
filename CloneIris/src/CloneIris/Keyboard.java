package CloneIris;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import processing.core.*;

import java.util.*;

@SuppressWarnings("serial")
public class Keyboard extends PApplet 
{
	private Poster[] posters;
	private int SCREEN_WIDTH, SCREEN_HEIGHT;
	
	//Poster Size
	public static final int POSTER_WIDTH = 135, POSTER_HEIGHT = 200;
	
	//UI Layout
	public static final int TOP_MARGIN = 200;
	public static final int LEFT_MARGIN = 300;
	
	public static final int HORIZONTAL_GAP = (int)(POSTER_WIDTH * 0.2f);
	public static final int VERTICAL_GAP = (int)(POSTER_HEIGHT * 0.2f);
	
	public static final int NUM_OF_POSTERS = 60;
	public static final int ROW = 5, COL = 12;
	
	//Animation Parameters
	private static final  int MAX_ROTATION_ANGLE_Y = 10;
	private static final  int MAX_ROTATION_ANGLE_X = 5;
	
	private float rotationAngle_Y = 0, rotationAngle_X = 0;
	private float distX = 0, stepX =5f, 
			rotationStepY = 1.2f, rotateBackstepY = 0.8f, depthstepX = 10;
	
	private float distY = 0, stepY = 5f, 
			rotationStepX = 1.2f, rotateBackstepX = 0.8f;
	
	
	private float depth = 150;

	private int current_selected_index = -1;
	private int current_selected_row = -1;
	private List<MouseEventListener> mouseEventList = new ArrayList<MouseEventListener>();
	private PGraphics buffer;
	
	private int pressCounter = 0;
	
	private boolean aPressed = false, dPressed = false, 
			wPressed = false, sPressed = false;
	private boolean isHoldingA = false, isHoldingD = false,
			isHoldingW = false, isHoldingS = false;
	private boolean fullScreen = true;
	
	private int rotationCenterX, rotationCenterY;
	private float centerX, centerY;
	private float percentageX = 0f, percentageY = 0f;;
	
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
		
		PImage texture = null;
		
		int x = 0;
		int y = 0;
		
		int startPosX = -1 * (COL * POSTER_WIDTH + (COL - 1) * HORIZONTAL_GAP) / 2;
		int startPosY = -1 * (ROW * POSTER_HEIGHT + (ROW - 1) * VERTICAL_GAP) / 2;
		int index = 0;
		
		
		
		for(int i = 0; i < ROW; i++)
		{
			for(int j = 0; j < COL; j++)
			{
				texture  = loadImage("poster/s"+(index+1)%9+".jpg");
				x = startPosX + (POSTER_WIDTH + HORIZONTAL_GAP) * j;
				y = startPosY + (POSTER_HEIGHT + VERTICAL_GAP) * i;
				
				posters[index] = new Poster(index, i, j, x, y, POSTER_WIDTH, POSTER_HEIGHT, texture, null);
				mouseEventList.add(posters[index]);
				index++;
			}
		}
		
		rotationCenterX = -startPosX;
		rotationCenterY = -startPosY;
	}
	
	public void draw()
	{
		background(100);
		smooth(2);
		stroke(110);
		if(aPressed==false)
		{
			if(rotationAngle_Y > 0)
			{
				rotationAngle_Y-=rotateBackstepY;
			}
		}
		
		if(dPressed==false)
		{
			if(rotationAngle_Y < 0)
			{
				rotationAngle_Y+=rotateBackstepY;
			}
		}
		
		if(wPressed == false)
		{
			if(rotationAngle_X < 0)
			{
				rotationAngle_X += rotateBackstepX;
			}
		}
		
		if(sPressed == false)
		{
			if(rotationAngle_X > 0)
			{
				rotationAngle_X -= rotateBackstepX;
			}
		}
		
		if(isHoldingA) 
		{
			distX += stepX;
			
			if(rotationAngle_Y < MAX_ROTATION_ANGLE_Y)
			{
				rotationAngle_Y+=rotationStepY;
			}
		}
		
		if(isHoldingD)
		{
			distX -= stepX;
			
			if(rotationAngle_Y > -MAX_ROTATION_ANGLE_Y)
			{
				rotationAngle_Y-=rotationStepY;
			}
		}
		
		if(isHoldingW)
		{
			distY += stepY;
			
			if(rotationAngle_X > -MAX_ROTATION_ANGLE_X)
			{
				rotationAngle_X-=rotationStepX;
			}
		}
		
		if(isHoldingS)
		{
			distY -= stepY;
			
			if(rotationAngle_X < MAX_ROTATION_ANGLE_X)
			{
				rotationAngle_X+=rotationStepX;
			}
		}
		
		
		translate(rotationCenterX, rotationCenterY, depth);
		
		rotateY(radians(rotationAngle_Y));
		rotateX(radians(rotationAngle_X));
		translate(-distX,distY);
		
		testHover();
		
		for(Poster p : posters)
		{
			p.draw(this.g, Poster.RenderMode.TEXTURE);
		}
	}
	
	private void testHover()
	{
		buffer.beginDraw();
			buffer.background(0);
			
			buffer.translate(rotationCenterX, rotationCenterY, depth);
			buffer.rotateY(radians(rotationAngle_Y));
			rotateX(radians(rotationAngle_X));
			buffer.translate(-distX,distY);
			
			for(Poster p : posters)
			{
				p.draw(buffer, Poster.RenderMode.FILL);
			}
		buffer.endDraw();
		
		int color = buffer.get(mouseX, mouseY) & 0xff;
		


		int id = getId(color);
		
//		if(mousePressed && (mouseButton == LEFT))
//		{
//			println("Color: " + color + " ID: " + id);
//		}
		
		if(id >= 0)
		{
			if(current_selected_index == - 101)	// edge case: on startup the default value of current_selected_index = -101
			{
				mouseEventList.get(id).onMouseEnter();
				current_selected_index = id;
			}
			
			if(current_selected_index!= id)
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
				
				mouseEventList.get(id).onMouseEnter();
				current_selected_index = id;
				current_selected_row = posters[id].getRow();
			}
		}
		else // user clicked on background
		{
			if(current_selected_index != id)
			{
				if(current_selected_index >=0)
				{
					mouseEventList.get(current_selected_index).onMouseLeave();
				}
				current_selected_index = id;
			}
		}
		
	}
	
	private int getId(int color)
	{
		return color - 101;
	}
	
	public void mouseWheel(int delta)
	{
		depth -= delta*depthstepX;
	}
	
	public void keyPressed()
	{
		
		if(key=='a')
		{
			aPressed = true;
			
			pressCounter++;
			
			if(pressCounter > 2)
			{
				isHoldingA = true;
			}
		}
		
		if(key=='d')
		{
			dPressed = true;

			pressCounter++;
			
			if(pressCounter > 2)
			{
				isHoldingD = true;
			}
			
			
		}
		
		if(key=='w')
		{
			wPressed = true;
			
			pressCounter++;
			
			if(pressCounter > 2)
			{
				isHoldingW = true;
			}
		}
		
		if(key=='s')
		{
			sPressed = true;
			
			pressCounter++;
			
			if(pressCounter > 2)
			{
				isHoldingS = true;
			}
		}

	}

	public void keyReleased()
	{
		
		if(key=='a')
		{
			aPressed = false;
			isHoldingA = false;
			
			if(pressCounter == 1)
			{
				distX += stepX;
			}
			pressCounter = 0;
		}
		
		if(key=='d')
		{
			dPressed = false;
			isHoldingD = false;
			
			if(pressCounter == 1)
			{
				distX -= stepX;
			}
			pressCounter = 0;
		}
		
		if(key=='w')
		{
			wPressed = false;
			isHoldingW = false;
			
			if(pressCounter == 1)
			{
				distY += stepY;
			}
			pressCounter = 0;
		}
		
		if(key=='s')
		{
			sPressed = false;
			isHoldingS = false;
			
			if(pressCounter == 1)
			{
				distY -= stepY;
			}
			pressCounter = 0;
		}
		
//		if(key=='k')
//		{
//			for(Poster p : posters)
//			{
//				println("id: " + p.getId() + " time: " + p.getHoverTime());
//			}
//		}
	}
	
	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--present", "CloneIris.Keyboard" });
	}
}
