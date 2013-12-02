package CloneIris;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import processing.core.*;

import java.util.*;

@SuppressWarnings("serial")
public class Mouse_RowSelection_FreeMove extends PApplet 
{
	private Poster[] posters;
	private Row[] rows;
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
	private static final  int MAX_ROTATION_ANGLE_Y = 5;
	private static final  int MAX_ROTATION_ANGLE_X = 3;
	
	private float rotationAngle_Y = 0, rotationAngle_X = 0;
	private float distX = 0, stepX =5f, 
			rotationStepY = 0.2f, rotateBackstepY = 0.2f, depthstepX = 10;
	
	private float distY = 0, stepY = 5f, 
			rotationStepX = 0.2f, rotateBackstepX = 0.2f;
	
	
	private float depth = 150;

	private int current_selected_index = -1;
	private int current_selected_row = -1;
	private List<MouseEventListener> mouseEventList = new ArrayList<MouseEventListener>();
	private PGraphics buffer;
	
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
		rows = new Row[ROW];
		
		PImage texture = null;
		
		int x = 0;
		int y = 0;
		
		int startPosX = -1 * (COL * POSTER_WIDTH + (COL - 1) * HORIZONTAL_GAP) / 2;
		int startPosY = -1 * (ROW * POSTER_HEIGHT + (ROW - 1) * VERTICAL_GAP) / 2;
		int index = 0;
		
		int rowWidth = COL * POSTER_WIDTH + (COL - 1) * HORIZONTAL_GAP + 10;
		int rowHeight = POSTER_HEIGHT + 10;
		
		
		for(int i = 0; i < ROW; i++)
		{
			rows[i] = new Row(i, startPosX, (startPosY + (POSTER_HEIGHT + VERTICAL_GAP) * i), rowWidth, rowHeight);
			
			for(int j = 0; j < COL; j++)
			{
				texture  = loadImage("poster/s"+(index+1)%9+".jpg");
				x = (POSTER_WIDTH + HORIZONTAL_GAP) * j;
				y = 0;
				
				posters[index] = new Poster(index, i, j, x, y, POSTER_WIDTH, POSTER_HEIGHT, texture, null);
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
		
		rotationCenterX = -startPosX;
		rotationCenterY = -startPosY;
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
		
		// Stop translate if users's focus on the center area of the screen

		if(Math.abs(percentageX) <= 0.3f) percentageX = 0f;
		if(Math.abs(percentageY) <= 0.2f) percentageY = 0f;
		
		
		translate(rotationCenterX, rotationCenterY, depth);
		
		// For rotation around the Y axis
		if(percentageX < 0)
		{
			int max = (int)Math.abs((MAX_ROTATION_ANGLE_Y * percentageX * 10));
			int temp = (int)(rotationAngle_Y * 10);
			
			if(temp > -(max))
			{
				temp -= 1;
			} 
			else if(temp < -(max))
			{
				temp += 1;
			}
			
			rotationAngle_Y = temp / 10f;

		}
		else if(percentageX > 0)
		{

			int max_angle = (int) (MAX_ROTATION_ANGLE_Y * percentageX  * 10);
			int temp = (int)(rotationAngle_Y * 10);

			if(temp < max_angle)
			{
				temp += 1;
			}
			else if(temp > max_angle)
			{
				temp -= 1;
			}
			
			rotationAngle_Y = temp / 10f;
		}
		else if(percentageX == 0)	// rotate back
		{
			if(rotationAngle_Y > 0)
			{
				rotationAngle_Y-=rotateBackstepY;
			}
			
			if(rotationAngle_Y < 0)
			{
				rotationAngle_Y+=rotateBackstepY;
			}
		}

		if(percentageY < 0)
		{
			int max_angle = (int) Math.abs((MAX_ROTATION_ANGLE_X * percentageY * 10));
			
			int temp = (int)(rotationAngle_X * 10);
			
			if(temp > -max_angle)
			{
				temp -= 1;
			}
			else if(temp < -max_angle)
			{
				temp += 1;
			}
			
			rotationAngle_X = temp / 10f;
			
		}
		else if(percentageY > 0)
		{
			int max_angle = (int) Math.abs((MAX_ROTATION_ANGLE_X * percentageY * 10));
			int temp = (int)(rotationAngle_X * 10);

			if(temp < max_angle)
			{
				temp += 1;
			}
			else if(temp > max_angle)
			{
				temp -= 1;
			}
			
			rotationAngle_X = temp / 10f;
		}
		else if(percentageY == 0)	// rotate back
		{
			if(rotationAngle_X > 0)
			{
				rotationAngle_X-=rotateBackstepX;
			}
			
			if(rotationAngle_X < 0)
			{
				rotationAngle_X+=rotateBackstepX;
			}
		}		
		
		//rotateY(radians(rotationAngle_Y));
		//rotateX(radians(rotationAngle_X));
		
	
		distX += stepX * percentageX;
		if(distX > 200) distX = 200;
		if(distX < - 200) distX = -200;
	
		distY += stepY * percentageY;
		
		if(distY > 100) distY = 100;
		if(distY < - 180) distY = -180;
		
		if(current_selected_row >=0 && current_selected_row <= (ROW - 1))
		{
			rows[current_selected_row].translate((stepX * percentageX), 0);
		}
		
		translate(0,distY);
		
		testHover();
		
//		for(Poster p : posters)
//		{
//			p.draw(this.g, Poster.RenderMode.TEXTURE);
//		}	
		
		for(Row r : rows)
		{
			r.draw(this.g, Poster.RenderMode.TEXTURE);
		}
	}
	
	private void testHover()
	{
		buffer.beginDraw();
			buffer.background(0);
			
			buffer.translate(rotationCenterX, rotationCenterY, depth);
//			buffer.rotateY(radians(rotationAngle_Y));
//			buffer.rotateX(radians(rotationAngle_X));
			buffer.translate(0,distY);
			
			for(Row r : rows)
			{
				r.draw(buffer, Poster.RenderMode.FILL);
			}
		buffer.endDraw();
		
		int color = buffer.get(mouseX, mouseY) & 0xff;	// 0xff changes singed int to unsigned int, it also extract the last 8 bits from the int in this case
		int rowColor = buffer.get(mouseX, mouseY) >> 16 & 0xff;

		int posterId = getId(color);
		int rowId = getId(rowColor);
		
		//println("posterId: " + posterId + " rowID: " + rowId);
		
		if(rowId >= 0 & rowId <= (ROW-1) )
		{
			//println("row id: " + rowId);
			if(current_selected_row != rowId)
			{
				if(current_selected_row != -1)	//Check for default value
				{
					mouseEventList.get(getRowIndex(current_selected_row)).onMouseLeave();
					
					current_selected_row = rowId;
					mouseEventList.get(getRowIndex(current_selected_row)).onMouseEnter();
				}
				else	
				{
					current_selected_row = rowId;
					mouseEventList.get(getRowIndex(current_selected_row)).onMouseEnter();
				}
			}
		}
		else
		{
			if(current_selected_row != -1 && rowId!=99)
			{
				for(Row r: rows)
				{
					r.onMouseLeave();
				}
				current_selected_row = -1;
			}
		}
		
		if(posterId >= 0)
		{
			
			if(current_selected_index == - 1)
			{
				mouseEventList.get(posterId).onMouseEnter();
				current_selected_index = posterId;
				
				current_selected_row = posters[posterId].getRow();
				mouseEventList.get(getRowIndex(current_selected_row)).onMouseEnter();
				
				for(int i = 0; i< rows.length; i++)
				{
					if(i != current_selected_row)
					{
						rows[i].onMouseLeave();
					}
				}
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
				
				if(current_selected_row >=0 && current_selected_row <= (ROW -1))
				{
					if(current_selected_row != posters[posterId].getRow())
					{
						for(Row r: rows)
						{
							r.onMouseLeave();
						}
					}
				}
				
				mouseEventList.get(posterId).onMouseEnter();
				current_selected_index = posterId;
				
				current_selected_row =  posters[posterId].getRow();		
				mouseEventList.get(getRowIndex(current_selected_row)).onMouseEnter();

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
	
	
	private int getId(int color)
	{
		return color - 101;
	}
	
	// In the mouseEventList, the rows are stored after all of the posters 
	private int getRowIndex(int id)
	{
		return NUM_OF_POSTERS + id;
	}
	
	public void mouseWheel(int delta)
	{
		depth -= delta*depthstepX;
	}
	
	public static void main(String[] args)
	{
		PApplet.main(new String[] { "--present", "CloneIris.Mouse_RowSelection_FreeMove" });
	}
}
