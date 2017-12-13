package com.daivd.chart.component.axis;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.Gravity;

import com.daivd.chart.data.ChartData;
import com.daivd.chart.data.BarData;
import com.daivd.chart.data.ScaleData;
import com.daivd.chart.exception.ChartException;

import java.util.List;

/**
 * Created by huangYanBin on 2017/9/26.
 * 横轴
 */

public class HorizontalAxis extends BaseAxis<String> {

    public HorizontalAxis() {
        direction = AxisDirection.BOTTOM;
    }
    private int rotateAngle= 0;
    private boolean isRotateAngle;
    private int textWidth;
    private int textHeight;
    private int rotateTextHeight;
    private boolean isShowFullValue; //是否显示全文字
    private ScaleData scaleData;

    @Override
    public void computeScale(ChartData<? extends BarData> chartData, Rect rect, Paint paint) {
        if(isDisplay()) {
             scaleData = chartData.getScaleData();
            scaleStyle.fillPaint(paint);
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            textHeight = (int) (fontMetrics.descent - fontMetrics.ascent);
            int maxLength = 0;
            String maxLengthXData = "1";
            for (String xData : chartData.getCharXDataList()) {
                String formatData = formatData(xData);
                if (maxLength < formatData.length()) {
                    maxLengthXData = formatData;
                    maxLength = formatData.length();
                }
            }
            textWidth = (int) paint.measureText(maxLengthXData);
            //计算旋转的高宽
            int dis = textHeight;
            if (isRotateAngle) {
                int tempHeight = (int) Math.abs(textWidth * Math.sin(rotateAngle * Math.PI / 180)
                        + textHeight * Math.cos(rotateAngle * Math.PI / 180));
                int tempWidth = (int) Math.abs(textWidth * Math.cos(rotateAngle * Math.PI / 180)
                        + textHeight * Math.sin(rotateAngle * Math.PI / 180));
                rotateTextHeight = tempHeight;
                dis += rotateTextHeight;
                textWidth = tempWidth;
            }
            dis += (int) (scaleStyle.getPadding() * 2 + axisStyle.getWidth());
            if (direction == AxisDirection.BOTTOM) {
                scaleData.scaleRect.bottom = dis;
            } else {
                scaleData.scaleRect.top = dis;
            }
        }
    }

    protected void drawScale(Canvas canvas, Rect zoomRect, Rect rect, Paint paint,  ChartData<? extends BarData> chartData) {

        ScaleData scaleData = chartData.getScaleData();
        List<String> groupDataList = chartData.getCharXDataList();
        int rowSize = scaleData.rowSize;
        int groupSize = groupDataList.size();
        if (groupSize != rowSize) {
            throw new ChartException("Horizontal Vertical axis data inconsistency");
        }
        float startY;
        if (direction == AxisDirection.BOTTOM) {
            startY = zoomRect.bottom -scaleData.scaleRect.bottom/2;
        } else {
            startY = zoomRect.top + scaleData.scaleRect.top/2;
        }
        int left = zoomRect.left ;
        int width = zoomRect.right - left;
        double perWidth = ((double) width) / (isLine? groupSize -1 : groupSize);
        int filterMultiple = (int) (textWidth / perWidth +1);
        for (int i = 0; i < groupSize; i++) {
            String content = groupDataList.get(i);
            int startX = getGravityStartX(left, i, perWidth);
            //留1px缓冲
            if (startX >= rect.left-1 && startX<= rect.right+1) {
                if( i % filterMultiple == 0) {
                    drawText(canvas, content,startX, startY,i, paint);
                    drawGrid(canvas, startX, rect, scaleData.scaleRect, paint);
                }
            }
        }
    }

    private int getGravityStartX(int left, int position, double perWidth) {
        int startX = (int) (left + position * perWidth);
        if (gravity == Gravity.CENTER) {
            startX += perWidth / 2;
        } else if (gravity == Gravity.RIGHT) {
            startX += perWidth;
        }
        return startX;
    }
    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas, String contentStr, int startX, float startY, int position, Paint paint) {
        String content = formatData(contentStr);
        scaleStyle.fillPaint(paint);
        paint.setTextAlign(Paint.Align.CENTER);
        if (isShowFullValue && position == 0) {
            int width = (int) paint.measureText(content);
            startX+= width/2;
        } else if (isShowFullValue && position == scaleData.rowSize - 1) {
            int width = (int) paint.measureText(content);
            startX-= width/2;
        }
        if (isRotateAngle) {
            canvas.save();
            canvas.rotate(rotateAngle, startX, startY);
            canvas.drawText(content, startX, startY + textHeight / 2, paint);
            canvas.restore();
        } else {
            canvas.drawText(content, startX, startY + textHeight / 2, paint);
        }
    }

    private String formatData(String data){
        return  getFormat()!= null ? getFormat().format(data) :data;
    }

    /**
     * 绘制网格
     */
    public void drawGrid(Canvas canvas, float startX, Rect rect, Rect scaleRect, Paint paint) {
        if (gridStyle != null && isDrawGrid) {
                gridStyle.fillPaint(paint);
                Path path = new Path();
                path.moveTo(startX, rect.top + scaleRect.top);
                path.lineTo(startX, rect.bottom - scaleRect.bottom);
                canvas.drawPath(path, paint);
        }
    }




    protected int[] calculation(Rect rect, Rect scaleRect) {
        int startX = rect.left + scaleRect.left;
        int endX = rect.right - scaleRect.right;
        int startY, endY;
        if (direction == AxisDirection.BOTTOM) {
            startY = rect.bottom - scaleRect.bottom;
        } else {
            startY = rect.top + scaleRect.top;
        }
        endY = startY;
        return new int[]{startX, startY, endX, endY};
    }


    @Override
    public void setAxisDirection(int axisDirection) {
        if (axisDirection == AxisDirection.BOTTOM || axisDirection == AxisDirection.TOP) {
            this.direction = axisDirection;
        } else throw new ChartException("Can only set BOTTOM, TOP direction");
    }


    /**
     * 设置文字旋转角度
     * @param rotateAngle
     */
    public void setRotateAngle(int rotateAngle) {
        isRotateAngle = true;
        this.rotateAngle = rotateAngle;
    }
    public boolean isShowFullValue() {
        return isShowFullValue;
    }

    public void setShowFullValue(boolean showFullValue) {
        isShowFullValue = showFullValue;
    }

}
