package com.socialbusiness.dev.orangebusiness.component;

public class PositionCalculator {

	/*
	 * 设定Y轴为0角度或360度，顺时针递增.
	 * angle:角度
	 * r:半径
	 */
	public static double[] getXy(float angle,int r){
		double[] xy=new double[2];

		if(angle==0 || angle==360){
			xy[0]=0;
			xy[1]=r;
		}
		else if(angle==90){
			xy[0]=r;
			xy[1]=0;
		}
		else if(angle==180){
			xy[0]=0;
			xy[1]=-r;
		}
		else if(angle==270){
			xy[0]=-r;
			xy[1]=0;
		}
		else{
			int qujian=0;
			float ang=angle;
			if(angle<90){
				qujian=0;
				xy[0]=getDuiBianChang(ang, r);
				xy[1]=getLinBianChang(ang, r);
			}
			else if(angle<180){
				qujian=1;
				ang=angle-90;
				xy[0]=getLinBianChang(ang, r);
				xy[1]=-getDuiBianChang(ang, r);
			}
			else if(angle<270){
				qujian=2;
				ang=angle-180;
				xy[0]=-getDuiBianChang(ang, r);
				xy[1]=-getLinBianChang(ang, r);
			}
			else if(angle<360){
				qujian=3;
				ang=angle-270;
				xy[0]=-getLinBianChang(ang, r);
				xy[1]=getDuiBianChang(ang, r);
			}
		}
		return xy;
	}
	
	public static double getLinBianChang(double angle,int r){
		double fudu=Math.PI*angle/180;
		double cos=Math.cos(fudu);
		return cos*r;
	}
	
	public static double getDuiBianChang(double angle,int r){
		double fudu=Math.PI*angle/180;
		double sin=Math.sin(fudu);
		return sin*r;
	}
	
	/*
	 * - (CGFloat)offsetForItemAtIndex:(NSInteger)index
{
    //calculate relative position
    CGFloat offset = index - _scrollOffset;
    if (_wrapEnabled)
    {
        if (offset > _numberOfItems/2)
        {
            offset -= _numberOfItems;
        }
        else if (offset < -_numberOfItems/2)
        {
            offset += _numberOfItems;
        }
    }
    
    //handle special case for one item
    if (_numberOfItems + _numberOfPlaceholdersToShow == 1)
    {
        offset = 0.0f;
    }
    
#ifdef ICAROUSEL_MACOS
    
    if (_vertical)
    {
        //invert transform
        offset = -offset;
    }
    
#endif
    
    return offset;
}
	 */
	public static double offsetForItemAtIndex(int index, int currentItemIndex, int numberOfItems) {
		double offset = index - currentItemIndex;
		//wrapEnabled = true
		if(offset > numberOfItems / 2) {
			offset -= numberOfItems;
		}
		else if(offset < -numberOfItems / 2) {
			offset += numberOfItems;
		}
		return offset;
	}
	
}
