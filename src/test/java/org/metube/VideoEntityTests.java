package org.metube;

import static junit.framework.TestCase.*;
import org.junit.Test;
import org.metube.entity.Video;

public class VideoEntityTests {

    @Test
    public void test_views_incrementMethod() {
        Video video = new Video();

        video.incrementViews();

        assertEquals("Increment views method does not work.", 1, video.getViews());
    }

    @Test
    public void test_getSummaryMethod() {
        Video video = new Video();

        video.setDescription("Tommy is going to the tennis court in one month. Then he will have fun.");

        assertEquals("Get summary method does not work.", "Tommy is going to the tennis court in one month. T...", video.getSummary());
    }
}
