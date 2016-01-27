package com.ivy.freport.layout;

import java.util.Arrays;

/**
 * 
 * 描述：
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年5月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author Steven.Zhu
 * @since
 */
public class Cell {

    private int      cellId;
    private String   value;
    private DataType dataType;
    private String   pattern;
    private boolean  hasParam;
    private int      width       = 10;
    private int      height      = 20;
    private int      colspan;
    private int      rowspan;
    private boolean  bold;
    private int      border;
    private String   borderColor = "#000000";
    private String   bgColor     = "#ffffff";
    private int      size        = 9;
    private int      align       = Align.LEFT.getValue();
    private int      valign      = Align.MIDDLE.getValue();

    private boolean  img;
    private float    imgHeight   = 100;
    private float    imgWidth    = 100;
    private byte[]   picBuffer;

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public String getValue() {
        return value;
    }

    public int getWidth() {
        return width;
    }

    public boolean isBold() {
        return bold;
    }

    public int getSize() {
        return size;
    }

    public int getAlign() {
        return align;
    }

    public int getValign() {
        return valign;
    }

    public boolean isHasParam() {
        return hasParam;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public int getColspan() {
        return colspan;
    }

    public int getHeight() {
        return height;
    }

    public boolean isImg() {
        return img;
    }

    public byte[] getPicBuffer() {
        return picBuffer;
    }

    public float getImgHeight() {
        return imgHeight;
    }

    public float getImgWidth() {
        return imgWidth;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setHasParam(boolean hasParam) {
        this.hasParam = hasParam;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public void setValign(int valign) {
        this.valign = valign;
    }

    public void setImg(boolean img) {
        this.img = img;
    }

    public void setImgHeight(float imgHeight) {
        this.imgHeight = imgHeight;
    }

    public void setImgWidth(float imgWidth) {
        this.imgWidth = imgWidth;
    }

    public void setPicBuffer(byte[] picBuffer) {
        this.picBuffer = picBuffer;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public int getRowspan() {
        return rowspan;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    @Override
    public String toString() {
        return "Cell [cellId=" + cellId
                + ", value="
                + value
                + ", dataType="
                + dataType
                + ", pattern="
                + pattern
                + ", hasParam="
                + hasParam
                + ", width="
                + width
                + ", height="
                + height
                + ", colspan="
                + colspan
                + ", rowspan="
                + rowspan
                + ", bold="
                + bold
                + ", border="
                + border
                + ", borderColor="
                + borderColor
                + ", bgColor="
                + bgColor
                + ", size="
                + size
                + ", align="
                + align
                + ", valign="
                + valign
                + ", img="
                + img
                + ", imgHeight="
                + imgHeight
                + ", imgWidth="
                + imgWidth
                + ", picBuffer="
                + Arrays.toString(picBuffer)
                + "]";
    }
}