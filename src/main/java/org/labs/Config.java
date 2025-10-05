package org.labs;

import java.time.Duration;

/**
 * @param N number of programmers
 * @param W number of waiters
 * @param F total portions in stock
 * @param thinkMin min thinking time in ms
 * @param thinkMax max thinking time in ms
 * @param eatMin min eating time in ms
 * @param eatMax max eating time in ms
 */
record Config(int N, int W, long F, int bitesInBowl, Duration thinkMin, Duration thinkMax, Duration eatMin, Duration eatMax) {}