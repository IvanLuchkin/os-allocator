import java.nio.ByteBuffer;
import java.util.Arrays;

public class MemoryAllocator implements Allocator {
    private final int size;
    private final byte[] memory;

    public MemoryAllocator(int size) {
        this.size = size;
        memory = new byte[size];
        initMemory();
    }

    private void initMemory() {
        memory[0] = getFalseInByte();
        setNewSize(0, size - 5);
    }

    private byte getFalseInByte() {
        return (byte) (0);
    }

    @Override
    public Integer mem_alloc(int size) {
        for (int i = 0; i < this.size; i++) {
            int lengthOfBlock = getLengthOfBlock(i);
            if (convertByteToBoolean(memory[i])) {
                i = i + 5 + lengthOfBlock;
                continue;
            }
            int sizeWithAlignment = getSizeWithAlignment(size);
            int nextHeaderIndex = getNextHeaderIndex(lengthOfBlock, i);
            if (lengthOfBlock >= size) {
                memory[i] = getTrueInByte();
                int sizeOfNewBlock = lengthOfBlock - size - 5;
                setNewSize(i, sizeWithAlignment);
                createNewHeaderAfterNewBlock(getNextHeaderIndex(getLengthOfBlock(i), i), sizeOfNewBlock);
                return i;
            }
            if (findNextUnoccupiedBlock(i, sizeWithAlignment, nextHeaderIndex))  {
                return i;
            }
        }
        return null;
    }

    private boolean isNextHeaderFree(int nextHeaderIndex) {
        return convertByteToBoolean(memory[nextHeaderIndex]);
    }

    private int getNextHeaderIndex(int lengthOfBlock, int i) {
        return i + 5 + lengthOfBlock;
    }

    private void setNewSize(int i, int sizeWithAlignment) {
        byte[] array = ByteBuffer.allocate(4).putInt(sizeWithAlignment).array();
        for (int j = 0; j < 4; j++) {
            memory[j + i + 1] = array[j];
        }
    }

    private byte getTrueInByte() {
        return (byte) 1;
    }

    private int getLengthOfBlock(int i) {
        return convertByteToInt(Arrays.copyOfRange(memory, i + 1, i + 5));
    }

    private int convertByteToInt(byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

    private boolean convertByteToBoolean(byte b) {
        return b != 0;
    }

    private int getSizeWithAlignment(int size) {
        return (int) (Math.ceil((size + 0.0) / ALIGNMENT_SIZE) * ALIGNMENT_SIZE);
    }

    private void createNewHeaderAfterNewBlock(int headerIndex, int sizeOfNewBlock) {
        memory[headerIndex] = getFalseInByte();
        setNewSize(headerIndex, sizeOfNewBlock - 5);
    }

    @Override
    public Integer mem_realloc(int address, int newSizeOfExistingBlock) {
        int realSize = getLengthOfBlock(address);
        newSizeOfExistingBlock = getSizeWithAlignment(newSizeOfExistingBlock);
        if (realSize == newSizeOfExistingBlock) {
            return address;
        } else if (realSize < newSizeOfExistingBlock) {
            int nextHeaderIndex = getNextHeaderIndex(realSize, address);
            if (findNextUnoccupiedBlock(address, newSizeOfExistingBlock, nextHeaderIndex)) {
                return address;
            }
            int newAddress = mem_alloc(newSizeOfExistingBlock);
            copyBlockToNewAddress(address, newAddress);
            memory[address] = getFalseInByte();
            return newAddress;
        } else {
            int alignmentBlocksThatCanBeRemoved = (realSize - newSizeOfExistingBlock) / ALIGNMENT_SIZE;
            if (alignmentBlocksThatCanBeRemoved > 0) {
                int sizeOfFreeMemory = alignmentBlocksThatCanBeRemoved * ALIGNMENT_SIZE;
                setNewSize(address, getLengthOfBlock(address) - sizeOfFreeMemory);
                createNewHeaderAfterNewBlock(getNextHeaderIndex(getLengthOfBlock(address), address), sizeOfFreeMemory);
                return address;
            }
        }
        return null;
    }

    private boolean findNextUnoccupiedBlock(int address, int newSizeOfExistingBlock, int nextHeaderIndex) {
        while (isNextHeaderFree(nextHeaderIndex)) {
            int newSize = getLengthOfBlock(nextHeaderIndex) + nextHeaderIndex - (address + 4);
            if (newSize > newSizeOfExistingBlock) {
                setNewSize(address, newSizeOfExistingBlock);
                createNewHeaderAfterNewBlock(nextHeaderIndex, newSize - newSizeOfExistingBlock);
                return true;
            }
            nextHeaderIndex = getNextHeaderIndex(getLengthOfBlock(nextHeaderIndex), nextHeaderIndex);
        }
        return false;
    }

    private void copyBlockToNewAddress(int header, int newAddress) {
        int lengthOfBlock = getLengthOfBlock(header);
        for (int i = 0; i < lengthOfBlock; i++) {
            memory[newAddress + 5 + i] = memory[header + 5 + i];
        }
    }

    @Override
    public void mem_free(int address) {
        memory[address] = getFalseInByte();
    }

    @Override
    public void dump() {
        printSeparatorLine();
        for (byte b : memory) {
            System.out.print(b + " ");
        }
        System.out.println();
        printSeparatorLine();
        System.out.println();
    }

    private void printSeparatorLine() {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<<=>>~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }
}
