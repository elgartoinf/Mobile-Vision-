package com.digitalonboarding;

/**
 * Created by DiegoCG on 3/02/17.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class VerticalTextView extends TextView
{
    /**
     * Esta Clase es utlizada para crear un Textview personalido
     * y que sea Vertical
     * Fue implementado en las vistas de las camara personalizadas
     */
    final boolean topDown;
    public VerticalTextView( Context context,
                             AttributeSet attrs )
    {
        super( context, attrs );
        final int gravity = getGravity();
        if ( Gravity.isVertical( gravity )
                && ( gravity & Gravity.VERTICAL_GRAVITY_MASK )
                == Gravity.BOTTOM )
        {
            setGravity(
                    ( gravity & Gravity.HORIZONTAL_GRAVITY_MASK )
                            | Gravity.TOP );
            topDown = false;
        }
        else
        {
            topDown = true;
        }
    }

    @Override
    protected void onMeasure( int widthMeasureSpec,
                              int heightMeasureSpec )
    {
        super.onMeasure( heightMeasureSpec,
                widthMeasureSpec );
        setMeasuredDimension( getMeasuredHeight(),
                getMeasuredWidth() );
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        TextPaint textPaint = getPaint();
        textPaint.setColor( getCurrentTextColor() );
        textPaint.drawableState = getDrawableState();

        canvas.save();

        if ( topDown )
        {
            canvas.translate( getWidth(), 0 );
            canvas.rotate( 270 );
        }
        else
        {
            canvas.translate( getWidth(), 0);
            canvas.rotate(90 );
        }

        canvas.translate( getCompoundPaddingLeft(),
                getExtendedPaddingTop() );

        getLayout().draw( canvas );
        canvas.restore();
    }

}