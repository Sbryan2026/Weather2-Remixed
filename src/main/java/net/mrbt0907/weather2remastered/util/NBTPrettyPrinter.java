package net.mrbt0907.weather2remastered.util;

import net.minecraft.nbt.*;

import java.util.Map;
import java.util.UUID;

public class NBTPrettyPrinter {

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String KEY = "\u001B[36m";     // Cyan
    private static final String STRING = "\u001B[32m";  // Green
    private static final String NUMBER = "\u001B[33m";  // Yellow
    private static final String BRACKETS = "\u001B[37m"; // White

    public static String prettyPrint(INBT nbt) {
        return prettyPrint(nbt, 0);
    }

    private static String prettyPrint(INBT nbt, int indent) {
        StringBuilder sb = new StringBuilder();
        String pad = repeat("  ", indent);

        if (nbt instanceof CompoundNBT) {
            sb.append(BRACKETS).append("{\n").append(RESET);
            CompoundNBT compound = (CompoundNBT) nbt;
            for (String key : compound.getAllKeys()) {
                sb.append(pad).append("  ")
                  .append(KEY).append(key).append(RESET)
                  .append(": ")
                  .append(prettyPrint(compound.get(key), indent + 1))
                  .append("\n");
            }
            sb.append(pad).append(BRACKETS).append("}").append(RESET);
        }
        else if (nbt instanceof ListNBT) {
            sb.append(BRACKETS).append("[\n").append(RESET);
            ListNBT list = (ListNBT) nbt;
            for (int i = 0; i < list.size(); i++) {
                sb.append(pad).append("  ").append(prettyPrint(list.get(i), indent + 1)).append("\n");
            }
            sb.append(pad).append(BRACKETS).append("]").append(RESET);
        }
        else if (nbt instanceof StringNBT) {
            sb.append(STRING).append(nbt.getAsString()).append(RESET);
        }
        else if (nbt instanceof NumberNBT) {
            sb.append(NUMBER).append(nbt.getAsString()).append(RESET);
        }
        else if (nbt instanceof IntArrayNBT) {
            int[] arr = ((IntArrayNBT) nbt).getAsIntArray();
            // Check if it's a UUID array
            if (arr.length == 4) {
                long most = ((long)arr[0] << 32) | (arr[1] & 0xFFFFFFFFL);
                long least = ((long)arr[2] << 32) | (arr[3] & 0xFFFFFFFFL);
                UUID uuid = new UUID(most, least);
                sb.append(STRING).append(uuid.toString()).append(RESET);
            } else {
                sb.append(NUMBER).append(java.util.Arrays.toString(arr)).append(RESET);
            }
        }
        else if (nbt instanceof ByteArrayNBT) {
            sb.append(NUMBER).append(java.util.Arrays.toString(((ByteArrayNBT) nbt).getAsByteArray())).append(RESET);
        }
        else if (nbt instanceof LongArrayNBT) {
            sb.append(NUMBER).append(java.util.Arrays.toString(((LongArrayNBT) nbt).getAsLongArray())).append(RESET);
        }
        else {
            sb.append(nbt.getAsString());
        }

        return sb.toString();
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
