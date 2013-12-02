package CloneIris;

import java.util.*;

import processing.core.PGraphics;
import CloneIris.Poster.RenderMode;

public class Row implements MouseEventListener
{
	private ArrayList<Poster> list;
	private int fillColor;
	private int id, w, h;
	private float x, y;
	private int color;
	private float distX, distY;
	
	public float getDistX() {
		return distX;
	}

	public void setDistX(float distX) {
		this.distX = distX;
	}

	public float getDistY() {
		return distY;
	}

	public void setDistY(float distY) {
		this.distY = distY;
	}

	public int getFillColor() {
		return fillColor;
	}

	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}
	public Row(int id, float x, float y, int w, int h)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		this.fillColor = id + 1;
		list = new ArrayList<Poster>();
		
		distX = 0;
		distY = 0;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void add(Poster p)
	{
		list.add(p);
	}
	
	public void draw(PGraphics g, RenderMode mode)
	{
		g.pushMatrix(); 
			
			g.translate(x, y);
			g.translate(distX, distY);
			g.noStroke();
			g.beginShape();
			
				if(mode == RenderMode.FILL)
				{
					g.fill(100+fillColor,0, 0);
					g.vertex(0, h);
					g.vertex(w, h);
					g.vertex(w, 0);
					g.vertex(0, 0);
				}
				else
				{
					g.fill(color,0, 0);
					g.vertex(0, h);
					g.vertex(w, h);
					g.vertex(w, 0);
					g.vertex(0, 0);
				}
				
			g.endShape();
			
			g.pushMatrix();
				for(Poster p : list)
				{
					p.draw(g, mode);
				}
			g.popMatrix();

	g.popMatrix();
	

	}
	
	public void translate(float deltaX, float deltaY)
	{
		x += deltaX;
		y += deltaY;
	}
	
	public String toString()
	{
		return "Row #" + id + " with " + list.size() + " elements";
	}

	@Override
	public void onMouseEnter() {
		//color = 255;
		//System.out.println("row " + id + " enter");
	}

	@Override
	public void onMouseLeave() {
		//color = 0;
		//System.out.println("row " + id + " left");
	}
}
