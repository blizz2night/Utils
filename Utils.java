/**
 * plane[0] buffer作为Y通道, planes[2] buffer作为UV通道
 * 为什么要用plane[2]的buffer?
 * @param image YUV_420_888
 * @param dst NV21
 */
public static boolean imageToNV21(Image image, byte[] dst) {
    Log.i(TAG, "imageToNV21: " + image);
    final int imageWidth = image.getWidth();
    final int imageHeight = image.getHeight();
    final Image.Plane[] planes = image.getPlanes();
    int offset = 0;
    for (int channel = 0; channel < planes.length; channel += 2) {
        Image.Plane plane = planes[channel];
        final ByteBuffer buffer = plane.getBuffer();
        final int rowStride = plane.getRowStride();
        // plane.getPixelStride()==2, 说明UV交错存储在buffer上
        final int pixelStride = plane.getPixelStride();
        // YUV420: UV数据是Y的1/2Height
        int rows = imageHeight / pixelStride;
        // 图像通道每行无填充对齐, 一次读完
        if (rowStride == imageWidth) {
            int length = imageWidth * rows;
            // (UV通道)最后一行的数据可能比图像宽度小(-1byte), 所以读buffer实际大小
            buffer.get(dst, offset, buffer.remaining());
            offset += length;
        } else {
            //图像每行有填充额外数据做字节对齐
            for (int i = 0; i < rows - 1; i++) {
                buffer.get(dst, offset, rowStride);
                offset += imageWidth;
            }
            // UV最后一行剩余的数据可能比rowStride小(-1byte)
            int lastRowLength = Math.min(imageWidth, buffer.remaining());
            buffer.get(dst, offset, lastRowLength);
            offset += imageWidth;
        }
    }
    return true;
}
