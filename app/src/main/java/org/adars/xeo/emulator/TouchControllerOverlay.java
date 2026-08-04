package org.adars.xeo.emulator;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * High-performance, low-latency modern Xbox 360 virtual controller overlay.
 * Features bulletproof pointer ID-locked multi-touch joystick tracking and layout persistence.
 */
public class TouchControllerOverlay extends View {

    public interface OnControllerInputListener {
        void onInputChanged(int buttons, int leftTrigger, int rightTrigger,
                            int lx, int ly, int rx, int ry);
    }

    // Xbox 360 button flags matching xenia/hid/input.h
    public static final int DPAD_UP = 0x0001;
    public static final int DPAD_DOWN = 0x0002;
    public static final int DPAD_LEFT = 0x0004;
    public static final int DPAD_RIGHT = 0x0008;
    public static final int START = 0x0010;
    public static final int BACK = 0x0020;
    public static final int LEFT_THUMB = 0x0040;
    public static final int RIGHT_THUMB = 0x0080;
    public static final int LEFT_SHOULDER = 0x0100;
    public static final int RIGHT_SHOULDER = 0x0200;
    public static final int GUIDE = 0x0400;
    public static final int A = 0x1000;
    public static final int B = 0x2000;
    public static final int X = 0x4000;
    public static final int Y = 0x8000;

    private OnControllerInputListener mInputListener;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Visibility / Opacity
    private boolean mControlsVisible = true;
    private float mOpacity = 0.50f; // 0.0 to 1.0

    // Customizable positions and sizes (normalized coordinates: 0.0 to 1.0)
    private float mLeftGroupX = 0.15f;
    private float mLeftGroupY = 0.70f;
    private float mLeftGroupScale = 1.0f;

    private float mRightGroupX = 0.85f;
    private float mRightGroupY = 0.70f;
    private float mRightGroupScale = 1.0f;

    private float mMiddleGroupX = 0.50f;
    private float mMiddleGroupY = 0.10f;
    private float mMiddleGroupScale = 1.0f;

    // Customization/Edit mode
    private boolean mEditMode = false;
    private int mSelectedGroup = -1; // 0=left, 1=right, 2=middle
    private float mDragLastX, mDragLastY;

    // Gamepad state
    private int mButtonsState = 0;
    private int mLeftTrigger = 0;
    private int mRightTrigger = 0;
    private int mLX = 0, mLY = 0;
    private int mRX = 0, mRY = 0;

    // Joystick current tracking coordinates (relative to centers)
    private float mLeftStickTrackX = 0, mLeftStickTrackY = 0;
    private float mRightStickTrackX = 0, mRightStickTrackY = 0;

    // Multi-touch tracking Pointer IDs
    private int mLeftStickPointerId = -1;
    private int mRightStickPointerId = -1;
    private int mDpadPointerId = -1;

    public TouchControllerOverlay(final Context context) {
        super(context);
        init();
    }

    public TouchControllerOverlay(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint.setStrokeWidth(3f);
        loadLayoutSettings();
    }

    private void loadLayoutSettings() {
        final SharedPreferences prefs = getContext().getSharedPreferences("TouchController", Context.MODE_PRIVATE);
        mLeftGroupX = prefs.getFloat("leftX", 0.15f);
        mLeftGroupY = prefs.getFloat("leftY", 0.70f);
        mLeftGroupScale = prefs.getFloat("leftScale", 1.0f);

        mRightGroupX = prefs.getFloat("rightX", 0.85f);
        mRightGroupY = prefs.getFloat("rightY", 0.70f);
        mRightGroupScale = prefs.getFloat("rightScale", 1.0f);

        mMiddleGroupX = prefs.getFloat("middleX", 0.50f);
        mMiddleGroupY = prefs.getFloat("middleY", 0.10f);
        mMiddleGroupScale = prefs.getFloat("middleScale", 1.0f);

        mOpacity = prefs.getFloat("opacity", 0.50f);
    }

    private void saveLayoutSettings() {
        final SharedPreferences prefs = getContext().getSharedPreferences("TouchController", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("leftX", mLeftGroupX)
                .putFloat("leftY", mLeftGroupY)
                .putFloat("leftScale", mLeftGroupScale)
                .putFloat("rightX", mRightGroupX)
                .putFloat("rightY", mRightGroupY)
                .putFloat("rightScale", mRightGroupScale)
                .putFloat("middleX", mMiddleGroupX)
                .putFloat("middleY", mMiddleGroupY)
                .putFloat("middleScale", mMiddleGroupScale)
                .putFloat("opacity", mOpacity)
                .apply();
    }

    public void setOnControllerInputListener(final OnControllerInputListener listener) {
        mInputListener = listener;
    }

    public void setControlsVisible(final boolean visible) {
        mControlsVisible = visible;
        invalidate();
    }

    public void setOpacity(final float opacity) {
        mOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        saveLayoutSettings();
        invalidate();
    }

    public void toggleEditMode() {
        mEditMode = !mEditMode;
        if (!mEditMode) {
            saveLayoutSettings();
        }
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (!mControlsVisible) return;

        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Draw active layouts
        mPaint.setAlpha((int) (255 * mOpacity));

        // --- Left Group (Left Stick & D-Pad) ---
        final float leftCenterX = mLeftGroupX * w;
        final float leftCenterY = mLeftGroupY * h;
        final float leftBaseRadius = 120f * mLeftGroupScale;
        drawLeftGroup(canvas, leftCenterX, leftCenterY, leftBaseRadius);

        // --- Right Group (Right Stick & Action Buttons A, B, X, Y) ---
        final float rightCenterX = mRightGroupX * w;
        final float rightCenterY = mRightGroupY * h;
        final float rightBaseRadius = 120f * mRightGroupScale;
        drawRightGroup(canvas, rightCenterX, rightCenterY, rightBaseRadius);

        // --- Middle Group (LB, RB, LT, RT, Back, Guide, Start) ---
        final float midCenterX = mMiddleGroupX * w;
        final float midCenterY = mMiddleGroupY * h;
        final float midBaseWidth = 350f * mMiddleGroupScale;
        drawMiddleGroup(canvas, midCenterX, midCenterY, midBaseWidth);

        // Draw edit boundaries if in customization mode
        if (mEditMode) {
            drawEditModeIndicators(canvas, w, h, leftCenterX, leftCenterY, leftBaseRadius,
                    rightCenterX, rightCenterY, rightBaseRadius, midCenterX, midCenterY, midBaseWidth);
        }
    }

    private void drawLeftGroup(final Canvas canvas, final float cx, final float cy, final float baseRadius) {
        // --- Left Joystick ---
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, baseRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb((int) (100 * mOpacity), 255, 255, 255));
        canvas.drawCircle(cx + mLeftStickTrackX, cy - mLeftStickTrackY, baseRadius * 0.4f, mPaint);

        // --- D-Pad ---
        final float dpadOffset = baseRadius * 1.8f;
        final float dcx = cx;
        final float dcy = cy + dpadOffset;
        final float dpadRadius = baseRadius * 0.8f;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(dcx, dcy, dpadRadius, mPaint);

        // Horizontal and vertical dividers
        canvas.drawLine(dcx - dpadRadius, dcy, dcx + dpadRadius, dcy, mPaint);
        canvas.drawLine(dcx, dcy - dpadRadius, dcx, dcy + dpadRadius, mPaint);

        // Highlight active dpad directions
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb((int) (120 * mOpacity), 0, 255, 0));
        if ((mButtonsState & DPAD_UP) != 0) {
            canvas.drawRect(dcx - dpadRadius * 0.3f, dcy - dpadRadius, dcx + dpadRadius * 0.3f, dcy, mPaint);
        }
        if ((mButtonsState & DPAD_DOWN) != 0) {
            canvas.drawRect(dcx - dpadRadius * 0.3f, dcy, dcx + dpadRadius * 0.3f, dcy + dpadRadius, mPaint);
        }
        if ((mButtonsState & DPAD_LEFT) != 0) {
            canvas.drawRect(dcx - dpadRadius, dcy - dpadRadius * 0.3f, dcx, dcy + dpadRadius * 0.3f, mPaint);
        }
        if ((mButtonsState & DPAD_RIGHT) != 0) {
            canvas.drawRect(dcx, dcy - dpadRadius * 0.3f, dcx + dpadRadius, dcy + dpadRadius * 0.3f, mPaint);
        }
    }

    private void drawRightGroup(final Canvas canvas, final float cx, final float cy, final float baseRadius) {
        // --- Right Joystick ---
        final float rightStickOffset = baseRadius * 1.8f;
        final float rcx = cx - rightStickOffset;
        final float rcy = cy;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(rcx, rcy, baseRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb((int) (100 * mOpacity), 255, 255, 255));
        canvas.drawCircle(rcx + mRightStickTrackX, rcy - mRightStickTrackY, baseRadius * 0.4f, mPaint);

        // --- Action Buttons A, B, X, Y ---
        final float buttonRadius = baseRadius * 0.35f;
        final float gap = baseRadius * 0.8f;

        // Button positions
        final float ax = cx;
        final float ay = cy + gap;

        final float bx = cx + gap;
        final float by = cy;

        final float xx = cx - gap;
        final float xy = cy;

        final float yx = cx;
        final float yy = cy - gap;

        // A Button
        mPaint.setStyle((mButtonsState & A) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(ax, ay, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(buttonRadius * 1.2f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("A", ax, ay + buttonRadius * 0.4f, mPaint);

        // B Button
        mPaint.setStyle((mButtonsState & B) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(bx, by, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText("B", bx, by + buttonRadius * 0.4f, mPaint);

        // X Button
        mPaint.setStyle((mButtonsState & X) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(xx, xy, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText("X", xx, xy + buttonRadius * 0.4f, mPaint);

        // Y Button
        mPaint.setStyle((mButtonsState & Y) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.YELLOW);
        canvas.drawCircle(yx, yy, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText("Y", yx, yy + buttonRadius * 0.4f, mPaint);
    }

    private void drawMiddleGroup(final Canvas canvas, final float cx, final float cy, final float baseWidth) {
        final float height = 80f * mMiddleGroupScale;
        final float buttonRadius = height * 0.4f;

        // Guide Button (center)
        mPaint.setStyle((mButtonsState & GUIDE) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.LTGRAY);
        canvas.drawCircle(cx, cy, buttonRadius * 1.3f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(buttonRadius * 0.8f);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("X", cx, cy + buttonRadius * 0.3f, mPaint);

        // Back Button (left of guide)
        mPaint.setStyle((mButtonsState & BACK) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(cx - baseWidth * 0.25f, cy, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText("<", cx - baseWidth * 0.25f, cy + buttonRadius * 0.35f, mPaint);

        // Start Button (right of guide)
        mPaint.setStyle((mButtonsState & START) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(cx + baseWidth * 0.25f, cy, buttonRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(">", cx + baseWidth * 0.25f, cy + buttonRadius * 0.35f, mPaint);

        // Left Bumper / Trigger
        final float bumperW = baseWidth * 0.45f;
        final float bumperH = height * 0.8f;
        final float leftBumperX = cx - baseWidth * 0.5f;
        final float rightBumperX = cx + baseWidth * 0.5f - bumperW;

        // Draw Bumpers
        mPaint.setStyle((mButtonsState & LEFT_SHOULDER) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(leftBumperX, cy + height, leftBumperX + bumperW, cy + height + bumperH), 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("LB", leftBumperX + bumperW * 0.5f, cy + height + bumperH * 0.65f, mPaint);

        mPaint.setStyle((mButtonsState & RIGHT_SHOULDER) != 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(rightBumperX, cy + height, rightBumperX + bumperW, cy + height + bumperH), 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("RB", rightBumperX + bumperW * 0.5f, cy + height + bumperH * 0.65f, mPaint);

        // Draw Triggers (Analog LT / RT)
        mPaint.setStyle(mLeftTrigger > 5 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(leftBumperX, cy + height * 2.1f, leftBumperX + bumperW, cy + height * 2.1f + bumperH), 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("LT", leftBumperX + bumperW * 0.5f, cy + height * 2.1f + bumperH * 0.65f, mPaint);

        mPaint.setStyle(mRightTrigger > 5 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(rightBumperX, cy + height * 2.1f, rightBumperX + bumperW, cy + height * 2.1f + bumperH), 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("RT", rightBumperX + bumperW * 0.5f, cy + height * 2.1f + bumperH * 0.65f, mPaint);
    }

    private void drawEditModeIndicators(final Canvas canvas, final int w, final int h,
                                         final float lcx, final float lcy, final float lrad,
                                         final float rcx, final float rcy, final float rrad,
                                         final float mcx, final float mcy, final float mwd) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(4f);

        // Boundary Left Group
        canvas.drawRect(lcx - lrad * 1.5f, lcy - lrad * 1.5f, lcx + lrad * 1.5f, lcy + lrad * 3.5f, mPaint);

        // Boundary Right Group
        canvas.drawRect(rcx - rrad * 3.2f, rcy - rrad * 1.5f, rcx + rrad * 1.5f, rcy + rrad * 1.5f, mPaint);

        // Boundary Middle Group
        canvas.drawRect(mcx - mwd * 0.6f, mcy - 50f, mcx + mwd * 0.6f, mcy + 300f, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(30f);
        canvas.drawText("DRAG GROUPS TO POSITION - DOUBLE TAP GUIDE BUTTON TO EXIT EDIT MODE", w * 0.5f, h - 50f, mPaint);
        mPaint.setStrokeWidth(3f);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!mControlsVisible) return false;

        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return false;

        final int action = event.getActionMasked();

        // Customization Drag & Edit handling
        if (mEditMode) {
            handleEditModeTouch(event, action, w, h);
            return true;
        }

        // Group screen-relative positions
        final float leftCenterX = mLeftGroupX * w;
        final float leftCenterY = mLeftGroupY * h;
        final float leftBaseRadius = 120f * mLeftGroupScale;

        final float rightCenterX = mRightGroupX * w;
        final float rightCenterY = mRightGroupY * h;
        final float rightBaseRadius = 120f * mRightGroupScale;

        final float midCenterX = mMiddleGroupX * w;
        final float midCenterY = mMiddleGroupY * h;
        final float midBaseWidth = 350f * mMiddleGroupScale;
        final float height = 80f * mMiddleGroupScale;
        final float bumperW = midBaseWidth * 0.45f;
        final float bumperH = height * 0.8f;

        // Check for pointer Down to lock D-pad / Joystick pointer IDs
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            final int activeIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(activeIndex);
            final float tx = event.getX(activeIndex);
            final float ty = event.getY(activeIndex);

            // Double tap Guide to enter edit mode
            final float buttonRadius = height * 0.4f;
            if (action == MotionEvent.ACTION_DOWN && Math.hypot(tx - midCenterX, ty - midCenterY) <= buttonRadius * 1.5f) {
                mEditMode = true;
                invalidate();
                return true;
            }

            // Lock Joysticks / Dpad by pointer IDs
            if (mLeftStickPointerId == -1 && Math.hypot(tx - leftCenterX, ty - leftCenterY) <= leftBaseRadius * 1.8f) {
                mLeftStickPointerId = pointerId;
            } else if (mRightStickPointerId == -1) {
                final float rightStickOffset = rightBaseRadius * 1.8f;
                final float rcx = rightCenterX - rightStickOffset;
                final float rcy = rightCenterY;
                if (Math.hypot(tx - rcx, ty - rcy) <= rightBaseRadius * 1.8f) {
                    mRightStickPointerId = pointerId;
                }
            }

            if (mDpadPointerId == -1) {
                final float dpadOffset = leftBaseRadius * 1.8f;
                final float dcx = leftCenterX;
                final float dcy = leftCenterY + dpadOffset;
                final float dpadRadius = leftBaseRadius * 0.8f;
                if (Math.hypot(tx - dcx, ty - dcy) <= dpadRadius) {
                    mDpadPointerId = pointerId;
                }
            }
        }

        // Release locked pointer IDs on Up
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            final int activeIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(activeIndex);

            if (pointerId == mLeftStickPointerId) {
                mLeftStickPointerId = -1;
                mLeftStickTrackX = 0;
                mLeftStickTrackY = 0;
                mLX = 0;
                mLY = 0;
            } else if (pointerId == mRightStickPointerId) {
                mRightStickPointerId = -1;
                mRightStickTrackX = 0;
                mRightStickTrackY = 0;
                mRX = 0;
                mRY = 0;
            } else if (pointerId == mDpadPointerId) {
                mDpadPointerId = -1;
            }
        }

        // Reset touch tracking button/triggers states for non-joystick buttons
        int newButtons = 0;
        int newLT = 0;
        int newRT = 0;

        // Perform evaluation of pointer coordinates
        final int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            if (action == MotionEvent.ACTION_POINTER_UP && i == event.getActionIndex()) {
                continue;
            }

            final int pointerId = event.getPointerId(i);
            final float tx = event.getX(i);
            final float ty = event.getY(i);

            // Update Left Joystick if locked
            if (pointerId == mLeftStickPointerId) {
                float dx = tx - leftCenterX;
                float dy = leftCenterY - ty;
                float dist = (float) Math.hypot(dx, dy);
                if (dist > leftBaseRadius) {
                    dx = (dx / dist) * leftBaseRadius;
                    dy = (dy / dist) * leftBaseRadius;
                }
                mLeftStickTrackX = dx;
                mLeftStickTrackY = dy;
                mLX = (int) ((dx / leftBaseRadius) * 32767);
                mLY = (int) ((dy / leftBaseRadius) * 32767);
                continue;
            }

            // Update Right Joystick if locked
            if (pointerId == mRightStickPointerId) {
                final float rightStickOffset = rightBaseRadius * 1.8f;
                final float rcx = rightCenterX - rightStickOffset;
                final float rcy = rightCenterY;
                float dx = tx - rcx;
                float dy = rcy - ty;
                float dist = (float) Math.hypot(dx, dy);
                if (dist > rightBaseRadius) {
                    dx = (dx / dist) * rightBaseRadius;
                    dy = (dy / dist) * rightBaseRadius;
                }
                mRightStickTrackX = dx;
                mRightStickTrackY = dy;
                mRX = (int) ((dx / rightBaseRadius) * 32767);
                mRY = (int) ((dy / rightBaseRadius) * 32767);
                continue;
            }

            // Update Dpad if locked
            if (pointerId == mDpadPointerId) {
                final float dpadOffset = leftBaseRadius * 1.8f;
                final float dcx = leftCenterX;
                final float dcy = leftCenterY + dpadOffset;
                final float dpadRadius = leftBaseRadius * 0.8f;
                float dx = tx - dcx;
                float dy = ty - dcy;
                if (Math.abs(dx) > dpadRadius * 0.2f) {
                    if (dx > 0) newButtons |= DPAD_RIGHT;
                    else newButtons |= DPAD_LEFT;
                }
                if (Math.abs(dy) > dpadRadius * 0.2f) {
                    if (dy > 0) newButtons |= DPAD_DOWN;
                    else newButtons |= DPAD_UP;
                }
                continue;
            }

            // --- Action Buttons A, B, X, Y ---
            final float buttonRadius = rightBaseRadius * 0.45f;
            final float gap = rightBaseRadius * 0.8f;

            if (Math.hypot(tx - rightCenterX, ty - (rightCenterY + gap)) <= buttonRadius) {
                newButtons |= A;
            } else if (Math.hypot(tx - (rightCenterX + gap), ty - rightCenterY) <= buttonRadius) {
                newButtons |= B;
            } else if (Math.hypot(tx - (rightCenterX - gap), ty - rightCenterY) <= buttonRadius) {
                newButtons |= X;
            } else if (Math.hypot(tx - rightCenterX, ty - (rightCenterY - gap)) <= buttonRadius) {
                newButtons |= Y;
            }

            // --- Middle / Upper Buttons ---
            // Guide, Back, Start
            final float midButtonRad = height * 0.6f;
            if (Math.hypot(tx - midCenterX, ty - midCenterY) <= midButtonRad * 1.5f) {
                newButtons |= GUIDE;
            } else if (Math.hypot(tx - (midCenterX - midBaseWidth * 0.25f), ty - midCenterY) <= midButtonRad) {
                newButtons |= BACK;
            } else if (Math.hypot(tx - (midCenterX + midBaseWidth * 0.25f), ty - midCenterY) <= midButtonRad) {
                newButtons |= START;
            }

            // Bumpers & Triggers
            final float leftBumperX = midCenterX - midBaseWidth * 0.5f;
            final float rightBumperX = midCenterX + midBaseWidth * 0.5f - bumperW;

            if (tx >= leftBumperX && tx <= leftBumperX + bumperW) {
                if (ty >= midCenterY + height && ty <= midCenterY + height + bumperH) {
                    newButtons |= LEFT_SHOULDER;
                } else if (ty >= midCenterY + height * 2.1f && ty <= midCenterY + height * 2.1f + bumperH) {
                    newLT = 255;
                }
            }

            if (tx >= rightBumperX && tx <= rightBumperX + bumperW) {
                if (ty >= midCenterY + height && ty <= midCenterY + height + bumperH) {
                    newButtons |= RIGHT_SHOULDER;
                } else if (ty >= midCenterY + height * 2.1f && ty <= midCenterY + height * 2.1f + bumperH) {
                    newRT = 255;
                }
            }
        }

        mButtonsState = newButtons;
        mLeftTrigger = newLT;
        mRightTrigger = newRT;

        // Dispatch input change to native layer
        if (mInputListener != null) {
            mInputListener.onInputChanged(mButtonsState, mLeftTrigger, mRightTrigger, mLX, mLY, mRX, mRY);
        }

        invalidate();
        return true;
    }

    private void handleEditModeTouch(final MotionEvent event, final int action, final int w, final int h) {
        final float tx = event.getX();
        final float ty = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            // Find selected group
            final float distToLeft = (float) Math.hypot(tx - mLeftGroupX * w, ty - mLeftGroupY * h);
            final float distToRight = (float) Math.hypot(tx - mRightGroupX * w, ty - mRightGroupY * h);
            final float distToMiddle = (float) Math.hypot(tx - mMiddleGroupX * w, ty - mMiddleGroupY * h);

            if (distToLeft < 200f) {
                mSelectedGroup = 0;
            } else if (distToRight < 250f) {
                mSelectedGroup = 1;
            } else if (distToMiddle < 300f) {
                mSelectedGroup = 2;
            } else {
                mSelectedGroup = -1;
            }

            mDragLastX = tx;
            mDragLastY = ty;
        } else if (action == MotionEvent.ACTION_MOVE && mSelectedGroup != -1) {
            final float dx = tx - mDragLastX;
            final float dy = ty - mDragLastY;

            if (event.getPointerCount() == 1) {
                if (mSelectedGroup == 0) {
                    mLeftGroupX = Math.max(0.05f, Math.min(0.95f, mLeftGroupX + dx / w));
                    mLeftGroupY = Math.max(0.05f, Math.min(0.95f, mLeftGroupY + dy / h));
                } else if (mSelectedGroup == 1) {
                    mRightGroupX = Math.max(0.05f, Math.min(0.95f, mRightGroupX + dx / w));
                    mRightGroupY = Math.max(0.05f, Math.min(0.95f, mRightGroupY + dy / h));
                } else if (mSelectedGroup == 2) {
                    mMiddleGroupX = Math.max(0.05f, Math.min(0.95f, mMiddleGroupX + dx / w));
                    mMiddleGroupY = Math.max(0.05f, Math.min(0.95f, mMiddleGroupY + dy / h));
                }
            } else if (event.getPointerCount() == 2) {
                final float scaleDiff = dy / h * 2.0f;
                if (mSelectedGroup == 0) {
                    mLeftGroupScale = Math.max(0.5f, Math.min(2.0f, mLeftGroupScale + scaleDiff));
                } else if (mSelectedGroup == 1) {
                    mRightGroupScale = Math.max(0.5f, Math.min(2.0f, mRightGroupScale + scaleDiff));
                } else if (mSelectedGroup == 2) {
                    mMiddleGroupScale = Math.max(0.5f, Math.min(2.0f, mMiddleGroupScale + scaleDiff));
                }
            }

            mDragLastX = tx;
            mDragLastY = ty;
            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            final float midCenterX = mMiddleGroupX * w;
            final float midCenterY = mMiddleGroupY * h;
            final float height = 80f * mMiddleGroupScale;
            final float buttonRadius = height * 0.4f;
            if (mSelectedGroup == 2 && Math.hypot(tx - midCenterX, ty - midCenterY) <= buttonRadius * 1.5f) {
                mEditMode = false;
                saveLayoutSettings();
            }
            mSelectedGroup = -1;
            invalidate();
        }
    }
}
