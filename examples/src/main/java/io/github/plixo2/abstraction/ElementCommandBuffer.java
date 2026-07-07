package io.github.plixo2.abstraction;


public final class ElementCommandBuffer extends CommandBuffer {


    private static final int PARAMS_PER_COMMAND = 5;
    public static final int BYTES_PER_COMMAND = PARAMS_PER_COMMAND * BYTES_PER_PARAM;

    private static final int ELEMENT_COUNT_OFFSET =  0 * BYTES_PER_PARAM;
    private static final int INSTANCE_COUNT_OFFSET = 1 * BYTES_PER_PARAM;
    private static final int FIRST_INDEX_OFFSET =    2 * BYTES_PER_PARAM;
    private static final int BASE_VERTEX_OFFSET =    3 * BYTES_PER_PARAM;
    private static final int BASE_INSTANCE_OFFSET =  4 * BYTES_PER_PARAM;

    public ElementCommandBuffer(int count, MemorySide generationSide) {
        super(count, BYTES_PER_COMMAND, generationSide);
    }

    public void setCompleteDraw(
            int index,

            int elementCount,
            int instanceCount,
            int firstIndex,
            int baseVertex,
            int baseInstance
    ) {
        setElementCount(index, elementCount);
        setInstanceCount(index, instanceCount);
        setFirstIndex(index, firstIndex);
        setBaseVertex(index, baseVertex);
        setBaseInstance(index, baseInstance);
    }

    public void setElementCount(int index, int count) {
        this.set(index, ELEMENT_COUNT_OFFSET, count);
    }

    public void setInstanceCount(int index, int count) {
        this.set(index, INSTANCE_COUNT_OFFSET, count);
    }

    public void setFirstIndex(int index, int firstIndex) {
        this.set(index, FIRST_INDEX_OFFSET, firstIndex);
    }

    public void setBaseVertex(int index, int vertex) {
        this.set(index, BASE_VERTEX_OFFSET, vertex);
    }

    public void setBaseInstance(int index, int instance) {
        this.set(index, BASE_INSTANCE_OFFSET, instance);
    }

}
