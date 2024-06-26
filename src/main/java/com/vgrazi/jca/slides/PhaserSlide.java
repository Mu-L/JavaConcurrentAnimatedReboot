package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.concurrent.Phaser;

@Component
public class PhaserSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    private Phaser phaser;

    public void run() {
        reset();
        threadContext.addButton("awaitAdvance(phase)", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("awaitAdvance");
            highlightSnippet(4);
            addRunnable(phaser, sprite, true);
        });

        threadContext.addButton("awaitAdvance(oldPhase)", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("awaitAdvance");
            highlightSnippet(4);
            addRunnable(phaser, sprite, false);
        });

        threadContext.addButton("arrive()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("arrive");
            highlightSnippet(1);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("arriveAndAwaitAdvance()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("arriveAndAwaitAdvance");
            highlightSnippet(3);
            addRunnable(phaser, sprite);
        });
        threadContext.addButton("arriveAndDeregister()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("arriveAndDeregister");
            highlightSnippet(2);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("register()", ()->{
            // todo: register should set a message on the UI message area, indicating the number
            //  of permits. No need to create a thread
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("register");
            highlightSnippet(5);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("bulkRegister()", ()->{
            // todo: register should set a message on the UI message area, indicating the number
            //  of permits. No need to create a thread
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("bulk-register");
            highlightSnippet(6);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("getPhase()", ()->{
            highlightSnippet(8);
            int phase = phaser.getPhase();
            displayPhaseAndPermits("");
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        threadCanvas.setThinMonolith();
        threadContext.setSlideLabel("Phaser");
        phaser = new Phaser(4) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                highlightSnippet(7);
                SwingUtilities.invokeLater(()-> displayPhaseAndPermits("onAdvance called."));
                return false;
            }
        };
        setSnippetFile("phaser.html");
    }

    private void addRunnable(Phaser phaser, ThreadSprite sprite) {
        // unused newPhase param
        addRunnable(phaser, sprite, false);
    }

    private void addRunnable(Phaser phaser, ThreadSprite sprite, boolean useCurrentPhase) {
        sprite.attachAndStartRunnable(() -> {
            int phase =0;
            while (sprite.isRunning()) {
                if ("release".equals(sprite.getAction())) {
                    threadContext.stopThread(sprite);
                    break;
                }
                switch (sprite.getAction()) {
                    case "awaitAdvance":
                        if (useCurrentPhase) {
                            // let's get the proper phase
                            phase = phaser.getPhase();
                        }
                        phase = phaser.awaitAdvance(phase);
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "arrive":
                        phase = phaser.arrive();
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "arriveAndAwaitAdvance":
                        phase = phaser.arriveAndAwaitAdvance();
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "arriveAndDeregister":
                        phase = phaser.arriveAndDeregister();
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "register":
                        phase = phaser.register();
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "bulk-register":
                        phase = phaser.bulkRegister(2);
                        displayPhaseAndPermits("");
                        sprite.setAction("release");
                        break;
                    case "default":
                        Thread.yield();
                        break;
                }
            }
            println(sprite + " exiting");
        }, true);
        threadContext.addSprite(sprite);
    }

    /**
     * Displays an option message (null if none) followed by the arrived and registered count
     * @param message
     */
    private void displayPhaseAndPermits(String message) {
        setMessage((message == null ? "" : message + " ") + "Phase: " + phaser.getPhase() + "; Arrived: " + phaser.getArrivedParties() + "; Registered: " + phaser.getRegisteredParties());
    }
}
