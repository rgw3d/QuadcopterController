package com.rgw3d.multiwiicontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

	private OnJoystickMoveListener onJoystickMoveListener; // Listener
	private int xPosition = 0; // Touch x position
	private int yPosition = 0; // Touch y position
	private double centerX = 0; // Center view x position
	private double centerY = 0; // Center view y position
	private Paint mainCircle;
	private Paint secondaryCircle;
	private Paint button;
	private Paint horizontalLine;
	private Paint verticalLine;
	public int joystickRadius;
	private int buttonRadius;
	private double lastAngle = 0;
	private double lastPower = 0;

	public JoystickView(Context context) {
		super(context);
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		initJoystickView();
	}

	protected void initJoystickView() {
		mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
		mainCircle.setColor(Color.WHITE);
		mainCircle.setStyle(Paint.Style.FILL_AND_STROKE);

		secondaryCircle = new Paint();
		secondaryCircle.setColor(Color.GREEN);
		secondaryCircle.setStyle(Paint.Style.STROKE);

		verticalLine = new Paint();
		verticalLine.setStrokeWidth(5);
		verticalLine.setColor(Color.RED);

		horizontalLine = new Paint();
		horizontalLine.setStrokeWidth(2);
		horizontalLine.setColor(Color.BLACK);

		button = new Paint(Paint.ANTI_ALIAS_FLAG);
		button.setColor(Color.RED);
		button.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		// before measure, get the center of view
		xPosition =  getWidth() / 2;
		yPosition = getWidth() / 2;
		int d = Math.min(xNew, yNew)/2;
		buttonRadius = (int) (d * 0.2);
		joystickRadius = (int) (d * 0.9);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int desiredWidth = 600;
        int desiredHeight = 600;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = parentWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, parentWidth);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = parentHeight;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, parentHeight);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

		setMeasuredDimension(width,height);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		centerX = (getWidth()) / 2;
		centerY = (getHeight()) / 2;

		// painting the main circle
		canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
				mainCircle);
		// painting the secondary circle
		canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
				secondaryCircle);
		// paint lines
		canvas.drawLine((float) centerX, (float) centerY, (float) centerX,
				(float) (centerY - joystickRadius), verticalLine);
		canvas.drawLine((float) (centerX - joystickRadius), (float) centerY,
				(float) (centerX + joystickRadius), (float) centerY,
				horizontalLine);
		canvas.drawLine((float) centerX, (float) (centerY + joystickRadius),
				(float) centerX, (float) centerY, horizontalLine);

		// painting the move button
		canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		xPosition = (int) event.getX();
		yPosition = (int) event.getY();
        if(xPosition< centerX-joystickRadius){
            xPosition = (int) centerX-joystickRadius;
        }else if(xPosition> centerX+joystickRadius){
            xPosition = (int) centerX+joystickRadius;
        }
        if(yPosition < centerY-joystickRadius){
            yPosition = (int) centerY-joystickRadius;
        }else if(yPosition > centerY+joystickRadius){
            yPosition = (int) centerY+joystickRadius;
        }
        invalidate();

		//double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
		//		+ (yPosition - centerY) * (yPosition - centerY));
		//if (abs > joystickRadius) {
		//	xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
		//	yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
		//}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			xPosition = (int) centerX;
			yPosition = (int) centerY;
            this.onJoystickMoveListener.onValueChanged(joystickRadius,xPosition,yPosition,(int)(centerX),(int)(centerY));
		}
        if (this.onJoystickMoveListener != null) {
            this.onJoystickMoveListener.onValueChanged(joystickRadius,xPosition, yPosition,(int)(centerX),(int)(centerY));
        }
		return true;
	}

	private double getAngle() {
        lastAngle = Math.toDegrees(Math.atan((yPosition-centerY)/(xPosition-centerX)));
        if(yPosition<=centerY) {
            if(xPosition>=centerX)
                lastAngle = Math.abs(lastAngle);
            else
                lastAngle = 180-lastAngle;
        }
        else{
            if(xPosition<=centerX)
                lastAngle = 180+Math.abs(lastAngle);
            else
                lastAngle = 360-lastAngle;
        }

        if(lastAngle%45<5 || lastAngle%45>40){
            lastAngle = 45 * Math.round(lastAngle/45);
        }
        return lastAngle;
	}

	private double getPower() {
		lastPower = (Math.sqrt((xPosition - centerX)
				* (xPosition - centerX) + (yPosition - centerY)
				* (yPosition - centerY)) / joystickRadius);
        if(lastPower>=0.95)
            lastPower=1;
        return lastPower;

	}

	public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
		this.onJoystickMoveListener = listener;
	}

	public interface OnJoystickMoveListener {
		void onValueChanged(int joystickR, int currentx, int currenty, int centerX, int centerY);
	}

}
