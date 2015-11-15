/*Author: Frederick Robson February 2015
 *  Allows the user to play a game of pong against the computer
 * 	Extension of breakout assignment for CS106A
 */
 
import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
 
public class Pong extends GraphicsProgram {
     
    //Application Constants
        /** Width and height of application window in pixels */
        public static final int APPLICATION_WIDTH = 400;
        public static final int APPLICATION_HEIGHT = 600;
     
    // Paddle constants
        /** The paddle dimensions*/
        private static final double PADDLE_WIDTH = 50;
        private static final double PADDLE_HEIGHT = 10;
        /** Distance of paddle from the edge of the screen*/
        private static final double PADDLE_Y_OFFSET = 50;
        /** Paddle color*/
        private static final Color PADDLE_COLOR = new Color(250,250,250);
        /** Instance variable for the player and computer paddle respectively */
        public GObject paddlePlayer = new GRect(0,0,PADDLE_WIDTH,PADDLE_HEIGHT);
        public GObject paddleComputer = new GRect(0,0,PADDLE_WIDTH,PADDLE_HEIGHT);
     
    // Ball constants
        /** Sets the radius of the ball*/
        private static final double BALL_RADIUS = 10;
        /** Sets the color of the ball*/
        private static final Color BALL_COLOR = new Color (250,250,250);
        /**Sets the speed of the ball in the y direction*/
        private static final double BALL_Y_SPEED = 2.0;
        /**Max change in x direction if you hit on the very edge of the paddle*/
        private static final double MAX_DX_CHANGE = 1.5;
        /** Max and min starting x speeds for the random x speed generator*/
        private static final double LOW_STARTING_DX = 1.0;
        private static final double HIGH_STARTING_DX = 3.0;
 
    //Computer paddle constants - Change to set the difficulty of the game
        /** How far the computer paddle moves towards the ball every frame*/
        private static final double DISTANCE_PER_FRAME = 1.2;
        /** The likelihood that the computer paddle will move in every frame*/
        private static final double PROBABILITY_OF_MOVING = 0.5;
     
    /** Pause time between frames*/
    private static final int PAUSE_TIME = 10;
     
    /** Number of points needed to win*/
    private static final int POINTS_TO_WIN = 2;
     
    public void run() {
        paddleSetUp(paddlePlayer,getHeight()-PADDLE_Y_OFFSET-PADDLE_HEIGHT);
        paddleSetUp(paddleComputer,PADDLE_Y_OFFSET);
        startingScreen();
        addMouseListeners();
        moveBall();
        run();
    }   
         
     
    public void mouseMoved(MouseEvent e){ 
        //The center x coordinate of the paddle
        double paddleX = e.getX() - (PADDLE_WIDTH/2);
        //The top y coordinate of the paddle
        double paddleY = getHeight()-PADDLE_Y_OFFSET;
        if(paddleOnScreen(e.getX())){
            paddlePlayer.setLocation(paddleX,paddleY);
        }
    }
    // Animates the ball and all of the changes of direction associated with the ball
    private void moveBall(){
        /** All the variables for the move ball method*/
            //The score of the player and the computer
            int playerScore =0;
            int computerScore =0;
            //The labels for the player and computers score
            GLabel playerScoreLabel = returnPlayerScores();
            add(playerScoreLabel);
            GLabel computerScoreLabel = returnComputerScores();
            add(computerScoreLabel);
            //The speed of the ball in the x and y direction
            double dx = randomDX();
            double dy = BALL_Y_SPEED;
            //The intercept is the point at which the ball will cross past the computer paddle
            double intercept = xScreenRatio(.5);
         
        GOval ball = ball(xScreenRatio(.5),yScreenRatio(.5));
        add(ball);
         
         
        while (playerScore<POINTS_TO_WIN && computerScore<POINTS_TO_WIN){
            ball.move(dx, dy);
            //Bounces the ball off the sides
            if(offScreenX(ballXCenter(ball))){
                dx*=-1;
                //Ensures that the ball does not get glued to the side
                while(offScreenX(ballXCenter(ball))){
                ball.move(dx, dy);  
                }
            }
            //Adds one to the score of the player if the ball goes off the top
            //Also resets the ball to the center of the screen and updates the score label
            if(offTop(ball.getY())){
                playerScore++;
                playerScoreLabel.setLabel("Player: "+playerScore);
                ballReset(ball,dx,dy);
                intercept = xScreenRatio(.5);
                dy = BALL_Y_SPEED;
            }
            //Adds one to the score of the computer if the ball goes off the bottom
            //Also resets the ball to the center of the screen and updates the score label
            if(offBottom(ball.getY())){
                computerScore++;
                computerScoreLabel.setLabel("Computer: "+computerScore);
                ball.setLocation(xScreenRatio(.5),yScreenRatio(.5));
                ballReset(ball,dx,dy);
            }
            //Bounces the ball off the player paddle
            //Also calculates where the ball will hit the computer paddle
            if(hitSomething(ballXCenter(ball),ballYCenter(ball))==paddlePlayer){
                dy*=-1;
                dx-=pointHitOnPaddle(paddlePlayer,ball);
                intercept = xIntercept(dx,dy,ballXCenter(ball),ballYCenter(ball));
                //This for loop ensures that the ball doesn't get stuck inside the paddle   
                for(int i = 0;i<(PADDLE_HEIGHT/(-dy))+1;i++){
                    ball.move(dx,dy);
                    pause(PAUSE_TIME);
                }
            }
            //Bounces the ball off the computer paddle
            //Also resets the intercept point to the middle of the screen
            if(hitSomething(ballXCenter(ball),ballYCenter(ball))==paddleComputer){
                dy*=-1;
                dx-=pointHitOnPaddle(paddleComputer,ball);
                intercept = xScreenRatio(.5);
                //This for loop ensures that the ball doesn't get stuck inside the paddle   
                for(int i = 0;i<(PADDLE_HEIGHT/(-dy))+1;i++){
                    ball.move(dx,dy);
                    pause(PAUSE_TIME);
                }
            }
            //Keeps moving the paddle so long as it is not already where the ball will hit it
            if(intercept!=paddleXCenter(paddleComputer)){
                movePaddleComputer(intercept);
            }
            pause(PAUSE_TIME);
        }
        endScreen(playerScore,computerScore,ball,computerScoreLabel,playerScoreLabel);
    }
    //Puts the paddle in the right location before the game has started
    private void paddleSetUp(GObject paddle, double y){
        double paddleX = (getWidth()-paddle.getWidth())/2;
        paddle.setLocation(paddleX,y);
        paddle.setColor(PADDLE_COLOR);
        add(paddle);
    }
    //Returns a ball centered at (x,y)
    private GOval ball(double x, double y){
        GOval ball = new GOval(x-BALL_RADIUS,y-BALL_RADIUS,BALL_RADIUS,BALL_RADIUS);
        ball.setFilled(true);
        ball.setColor(BALL_COLOR);
        return ball;
    }
    //Checks whether the ball is on screen in the x direction, using the x coordinate of the center of the ball
    private boolean offScreenX(double x){
        if(x-BALL_RADIUS<=0 || x+BALL_RADIUS>=getWidth()){
            return true;
        }else{
            return false;
        }
    }
    //Returns the x coordinate, for example half way across the screen if n=.5.
    private double xScreenRatio(double n){
        double result = getWidth()*n;
        return result;
    }
    //Returns the y coordinate, for example half way across the screen if n=.5.
    private double yScreenRatio(double n){
        double result = getHeight()*n;
        return result;
    }
    //Randomly generates the x direction of the ball
    private double randomDX(){
        RandomGenerator rgen = RandomGenerator.getInstance();
        double result = rgen.nextDouble(LOW_STARTING_DX,HIGH_STARTING_DX);
        if(rgen.nextBoolean()){
            result*=-1;
        }
        return result;
    }
    //Returns a true false if the ball has hit anything(which could only be the paddle)
    private GObject hitSomething(double x,double y){
        GObject result = null;
        GObject objectHitBottomRight = getElementAt(x+BALL_RADIUS,y+BALL_RADIUS);
        if(objectHitBottomRight != null){
            result = objectHitBottomRight;
        }else{
            GObject objectHitTopRight = getElementAt(x+BALL_RADIUS,y-BALL_RADIUS);
            if(objectHitTopRight != null){
                result = objectHitTopRight;
            }else{
                GObject objectHitBottomLeft = getElementAt(x-BALL_RADIUS,y+BALL_RADIUS);
                if(objectHitBottomLeft != null){
                    result = objectHitBottomLeft;
                }else{
                    GObject objectHitTopLeft = getElementAt(x-BALL_RADIUS,y-BALL_RADIUS);
                    if(objectHitTopLeft != null){
                        result = objectHitTopLeft;
                    }
                }   
            }
        } 
        return result;
    }
    //Tells if the ball goes off the top
    private boolean offTop(double y){
        if(y+BALL_RADIUS<0){
            return true;
        }else{
            return false;
        }
    }
    //Returns true if the ball has traveled off the bottom of the screen, using y coordinate of ball center
    private boolean offBottom(double y){
        if(y-BALL_RADIUS>getHeight()) return true;
        return false;
    }
    //Returns the x coordinate of the center of the ball
    private double ballXCenter(GObject ball){
        return ball.getX() + BALL_RADIUS;
    }
    //Returns the y coordinate of the center of the ball
    private double ballYCenter(GObject ball){
        return ball.getY() + BALL_RADIUS;
    } 
    //Returns the x coordinate the ball will pass by the computer paddle
    private double xIntercept(double dx, double dy, double ballX, double ballY){
        GOval predictedBall = ball(ballX,ballY);
        while(ballYCenter(predictedBall)>PADDLE_Y_OFFSET+PADDLE_HEIGHT){
            predictedBall.move(dx,dy);
            if(offScreenX(ballXCenter(predictedBall))){
                dx*=-1;
            }
        }
        return ballXCenter(predictedBall);
    }
    //Moves the paddle to the intercept, with randomness in order to simulate an opponent
    private void movePaddleComputer(double intercept){
        int leftOrRight = 1;
        if(paddleXCenter(paddleComputer)>intercept){
            leftOrRight = -1;
        }
        RandomGenerator rgen = RandomGenerator.getInstance();
        if(rgen.nextBoolean(PROBABILITY_OF_MOVING)){
            paddleComputer.move(DISTANCE_PER_FRAME*leftOrRight,0);
        }
    }
    //Returns the x coordinate of the center of the paddle
    private double paddleXCenter(GObject object){
        return object.getX()+(PADDLE_WIDTH)/2;
    }
    //Returns a boolean value for whether the paddle is on the screen
    private boolean paddleOnScreen(double x){
        if(x>=(PADDLE_WIDTH/2) && x<=getWidth()-(PADDLE_WIDTH/2)) return true;
        return false;
    }
    //returns how far from the center of the paddle the ball hits
    private double pointHitOnPaddle(GObject paddle, GObject ball){
        double distanceFromCenter = paddleXCenter(paddle)-ballXCenter(ball);
        return (distanceFromCenter/(PADDLE_WIDTH/2))*MAX_DX_CHANGE;
    }
    //Returns a generic label written in the color white
    private GLabel label(String s,double x, double y){
        GLabel label = new GLabel(s,x,y); 
        label.setColor(Color.WHITE);
        return label;
    }
    //Returns a label with the player's score
    private GLabel returnPlayerScores(){ 
        GLabel playerScoreLabel = label("Player: 0",0,0);
        playerScoreLabel.setLocation(0,getHeight()-playerScoreLabel.getDescent());
        return playerScoreLabel;
    }
    //Returns a label with the computer score
    private GLabel returnComputerScores(){
        GLabel computerScoreLabel = label("Computer: 0",0,0);
        computerScoreLabel.setLocation(0,computerScoreLabel.getHeight());
        return computerScoreLabel;
    }
    //Resets the ball to the center of the screen after the it goes off the end of screen
    private void ballReset(GObject ball, double dx, double dy){
        ball.setLocation(xScreenRatio(.5),yScreenRatio(.5));
        waitForClick();
        dx = randomDX();
    }
    //Sets up the starting screen with label telling you how many points you need to score to win
    private void startingScreen(){
        setBackground(Color.BLACK);
        GLabel openingScreenLabel = label("Welcome to Pong. Score "+POINTS_TO_WIN+" points to win. Click to start.",0,0);
        double openingScreenLabelX = (getWidth()-openingScreenLabel.getWidth())/2;
        double openingScreenLabelY = (getHeight()-openingScreenLabel.getHeight())/2;
        openingScreenLabel.setLocation(openingScreenLabelX,openingScreenLabelY);
        add(openingScreenLabel);
        waitForClick();
        remove(openingScreenLabel);
    }
    //Removes the ball and score labels in preparation for a new game
    //Also tells you whether or not you have won with a centered message
    private void endScreen(int playerScore, int computerScore, GObject ball, GLabel computerScoreLabel, GLabel playerScoreLabel){
        remove(ball);
        remove(computerScoreLabel);
        remove(playerScoreLabel);
        GLabel endScreenLabel = label("",0,0);
        if(playerScore == POINTS_TO_WIN){
            endScreenLabel.setLabel("You win! Click to play again");
        }
        if(computerScore == POINTS_TO_WIN){
            endScreenLabel.setLabel("You Lose :( Click to play again");
        }
        double endScreenLabelX = (getWidth()-endScreenLabel.getWidth())/2;
        double endScreenLabelY = (getHeight()-endScreenLabel.getHeight())/2;
        endScreenLabel.setLocation(endScreenLabelX,endScreenLabelY);
        add(endScreenLabel);
        waitForClick();
        remove(endScreenLabel);
    }
}
