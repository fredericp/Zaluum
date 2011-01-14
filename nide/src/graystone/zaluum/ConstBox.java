package graystone.zaluum;

import graystone.zaluum.annotations.Box;
import graystone.zaluum.annotations.Out;

@Box(image="graystone/zaluum/const.png")
public class ConstBox {
  @Out(x=10,y=10) public double o = 1.0;
  public void apply(){
    System.out.println("const" + o);
  }
}