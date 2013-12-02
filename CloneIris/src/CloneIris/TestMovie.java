package CloneIris;

import processing.video.*;
import processing.core.*;

import java.lang.reflect.*;


public class TestMovie extends PApplet 
{
	private Movie movie;
	
	public void setup()
	{
		size(640*2, 360*2, P2D);
		movie = new Movie(this, "movie/2.mov");
		Method method, method2;
		try {
			method = this.getClass().getMethod("movieEvent", new Class[] { Movie.class });
			//method2 = this.getClass().getMethod("nothing", null);
			println(method.toString());
			//println(method2.toString());
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void nothing(){}
	
	public void draw()
	{
		background(0);
		image(movie, 0, 0, 848, 360);
	}
	
	public void movieEvent(Movie m) 
	{
		println("called");
		if(m == this.movie)
		{
		  m.read();
		  println(m);
		}
	}
	
	public void mousePressed()
	{
		movie.play();
		println("play");
	}
}
