package CloneIris;
import processing.core.*;
import processing.video.*;


public class Poster implements MouseEventListener
{
	private PImage texture;
	private int x,y,w,h;
	private int id;
	private int fillColor;
	private float scale = 1.0f, scaleStep = 0.01f;
	private boolean mouseHover = false;
	private long hoverDuration;
	private long startTime;
	private int row, col;
	private Movie m;
	private static final int HOVER_DURATION_MS = 150;
	
	public Movie getMovie() {
		return m;
	}
	
	public void setMovie(Movie m)
	{
		this.m = m;
	}

	public int getRow() {
		return row;
	}


	public enum RenderMode
	{
		TEXTURE,
		FILL,
		NONE
	}
	
	public int getFillColor() {
		return fillColor;
	}

	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}

	public Poster()
	{
		id = -1;
		x = 0;
		y = 0;
		w = 0;
		h = 0;
		
		texture = null;
		fillColor = 0;
		hoverDuration = 0;
		
		m = null;
	}
	
	public Poster(int id, int row, int col, int x, int y, int w, int h, PImage texture, Movie m)
	{
		this.id = id;
		this.row = row;
		this.col = col;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.texture = texture;
		fillColor = id + 1;
		hoverDuration = 0;
		this.m = m;
	}
	
	public String toString()
	{
		return String.format("id = %s \n x: %s y: %s width: %s height: %s\n Texture width: %s height: %s", 
				id, x , y, w, h, texture.width, texture.height);
	}
	
	public void draw(PGraphics g, RenderMode mode)
	{
		g.pushMatrix(); 
		    g.translate(x+w/2,y+h/2);
		    
		    if(mouseHover && getHoverTime() >= HOVER_DURATION_MS)
		    {
		    	scale += scaleStep;
		    }
		    else
		    {
		    	scale -= scaleStep;
		    }
		    
		    if(scale > 1.2f)
		    {
		    	scale = 1.2f;
		    }
		    
		    if(scale < 1)
		    {
		    	scale = 1f;
		    }
		    
			g.scale(scale);
			g.translate(-x-w/2,-y-h/2);
			
			g.translate(x,y);
			
			g.beginShape();
			
				if(mode == RenderMode.TEXTURE)
				{
					if(texture!=null)
					{
						g.textureMode(PConstants.NORMAL);
						g.texture(texture);
					}
					else
					{
						g.fill(200,200, 100+fillColor);
					}
				}
				else
				{
					g.fill(200,200, 100+fillColor);
				}
				
				g.vertex(0, h, 0,1);
				g.vertex(w, h, 1, 1);
				g.vertex(w, 0, 1,0);
				g.vertex(0, 0, 0, 0);
				
			g.endShape();
		g.popMatrix();
	}
	


	@Override
	public void onMouseEnter() 
	{
		mouseHover = true;
		//System.out.println("id: " + id + " entered");
		
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onMouseLeave() 
	{
		mouseHover = false;
		//System.out.println("id: " + id + " released");
		hoverDuration += System.currentTimeMillis() - startTime;
	}
	
	public long getHoverTime()
	{
		if(startTime >= 0)
		{
			return System.currentTimeMillis() - startTime;
		}
		else
		{
			return -1;
		}
	}
	
	public int getId()
	{
		return id;
	}
}
