package io.github.currentbrick.generation;

public class DummyNoise {
    /**
     * Generates a repeatable 2D pseudo-random smooth noise value between -1.0 and 1.0.
     * Perfect for infinite generation without external libraries.
     */
    public static float generate(float x, float y, int seed) {
        // Layer 1: Broad continental terrain shapes
        float n1 = noise(x, y, seed);
        // Layer 2: Medium hills (doubled frequency, halved intensity)
        float n2 = noise(x * 2.0f, y * 2.0f, seed) * 0.5f;
        // Layer 3: Fine jagged details
        float n3 = noise(x * 4.0f, y * 4.0f, seed) * 0.25f;

        // Combine and normalize to fit clean bounds
        return (n1 + n2 + n3 / 1.75f);
    }

    private static float noise(float x, float y, int seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fx = x - ix;
        float fy = y - iy;

        // Smoothstep interpolation curves
        float ux = fx * fx * (3.0f - 2.0f * fx);
        float uy = fy * fy * (3.0f - 2.0f * fy);

        float a = hash(ix, iy, seed);
        float b = hash(ix + 1, iy, seed);
        float c = hash(ix, iy + 1, seed);
        float d = hash(ix + 1, iy + 1, seed);

        return lerp(uy, lerp(ux, a, b), lerp(ux, c, d));
    }

    private static float hash(int x, int y, int seed) {
        // Deterministic pseudo-random number generator math
        long h = (long)x * 0x27d4eb2dL + (long)y * 0x9111f63dL + (long)seed * 0x45d9f3bL;
        h = (h ^ (h >> 15)) * 0xcd7dcd7dL;
        h = (h ^ (h >> 13)) * 0x8b3dd84dL;
        return ((h ^ (h >> 16)) & 0x7fffffff) / (float) 0x7fffffff * 2.0f - 1.0f;
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
}
