package io.github.plixo2.abstraction;



public final class ArrayCommandBuffer extends CommandBuffer {

    private static final int PARAMS_PER_COMMAND = 4;
    public static final int BYTES_PER_COMMAND = PARAMS_PER_COMMAND * BYTES_PER_PARAM;

    private static final int COUNT_OFFSET =  0 * BYTES_PER_PARAM;
    private static final int INSTANCE_COUNT_OFFSET = 1 * BYTES_PER_PARAM;
    private static final int FIRST_OFFSET =    2 * BYTES_PER_PARAM;
    private static final int BASE_INSTANCE_OFFSET =  3 * BYTES_PER_PARAM;

    public ArrayCommandBuffer(int count, MemorySide generationSide) {
        super(count, BYTES_PER_COMMAND, generationSide);
    }

    public void setCompleteDraw(
            int index,

            int count,
            int instanceCount,
            int first,
            int baseInstance
    ) {
        setCount(index, count);
        setInstanceCount(index, instanceCount);
        setFirst(index, first);
        setBaseInstance(index, baseInstance);
    }

    public void setCount(int index, int count) {
        this.set(index, COUNT_OFFSET, count);
    }

    public void setInstanceCount(int index, int count) {
        this.set(index, INSTANCE_COUNT_OFFSET, count);
    }

    public void setFirst(int index, int firstIndex) {
        this.set(index, FIRST_OFFSET, firstIndex);
    }

    public void setBaseInstance(int index, int instance) {
        this.set(index, BASE_INSTANCE_OFFSET, instance);
    }


}
