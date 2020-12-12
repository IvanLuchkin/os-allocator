public class Main {

    public static void main(String[] args) {
        System.out.println("Create new allocator");
        Allocator allocator = new MemoryAllocator(30);
        allocator.dump();

        System.out.println("Allocate a block of memory");
        allocator.mem_alloc(9);
        allocator.dump();

        System.out.println("Reallocate block");
        allocator.mem_realloc(0, 7);
        allocator.dump();

        System.out.println("Mark the block as free");
        allocator.mem_free(0);
        allocator.dump();
    }
}
