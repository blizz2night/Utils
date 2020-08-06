class Utils{
    /**
     * plane[1] ByteBuffer从U开始, plane[2] ByteBuffer从V开始
     * 如果UV交错存储,理论上应该有结构
     *         NV21   buffer= [V, U, V, U]
     *       plane[1] buffer= [   U, V, U]
     *       plane[2] buffer= [V, U, V,  ]
     *
     * 注意
     * 1. 如上述两个buffer比原始NV21小1byte的情况,
     *  直接使用plane[2]的buffer会丢失末尾一个字节的U
     *  todo 考虑取plane[1] 补偿dst[dst.length-1]
     * 2. 不兼容NV12,I420,YV12
     *         NV12   buffer= [U, V, U, V]
     *       plane[1] buffer= [U, V, U,  ]
     *       plane[2] buffer= [   V, U, V]
     *
     * @param image Image对象，{@link ImageFormat#YUV_420_888}
     * @param dst  返回NV21格式数据
     */
    public static boolean imageToNV21(Image image, byte[] dst) {
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
            // YUV420: UV数据是Y的1/2
            int rows = imageHeight / pixelStride;
            // 图像通道每行无填充对齐, 一次读完
            if (rowStride == imageWidth) {
                // Copy whole plane from buffer into |data| at once.
                int length = rowStride * rows;
                // plane[2]的buffer比实际图像VU小1byte,读buffer实际大小
                buffer.get(dst, offset, buffer.remaining());
                offset += length;
            } else {
                //图像每行有填充额外数据做字节对齐
                for (int i = 0; i < rows - 1; i++) {
                    buffer.get(dst, offset, rowStride);
                    offset += imageWidth;
                }
                //plane[2]的buffer比实际图像VU小1byte,最后一行读buffer实际大小
                int lastRowLength = Math.min(imageWidth, buffer.remaining());
                buffer.get(dst, offset, lastRowLength);
                offset += imageWidth;
            }
        }
        return true;
    }
}
