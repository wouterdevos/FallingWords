# FallingWords

## Time Distribution

* Total time (7h 44m)

* Concept (44m)

* Model layer (1h)

* View (4h 30m)

* Game mechanics (1h 30m)

## Decisions made to solve certain aspects of the game

I chose to use a custom view as it gave me the most flexibility when implementing the game view. I limited the number of falling words to 4 words per game so that a single round wouldn't take any excessive amount of time to complete. Instead of having each word fall down one at a time, I thought the game play would be more exciting for the player if 4 words fell down in a staggered order. This way the player has to act quickly and also consider multiple options at the same time. 

I struggled to figure out what sort of scoring system I wanted to use but eventually I decided that the player would get 1 point for selecting the correct word and -1 point for selecting an incorrect word. The player's score can not go below 0. I thought this game machanic would discourage the player from guessing and also give them the opportunity to try again if they made a mistake on a previous selection.

I placed the test word at the top of the screen so that the maximum space would be available for the falling words. This also ensures that the falling words do not overlap with the test word, as would be the case if the test word was in the center of the screen. 

The falling words were spawned from two points for simplicity. Furthermore, this makes the most of the available space on the screen and also makes it more challenging for the player as they have to look to different parts of the screen. The falling words were staggered so that they would be completely visible to the player.

I placed the score at the bottom of the screen because it was too cluttered at the top of the screen next to the test word. The gray rectangle at the bottom of the screen helps to highlight the player's score. I decided that it would be easiest to give the player visual feedback about a correct/incorrect answer by changing the colour of the bottom rectangle to green/red and then fading back to the gray colour. Most players recognize that green provides positive feedback and red provides negative feedback, so these colours were an obvious choice.

## Decisions made because of restricted time

To save time I used the ValueAnimator api for the falling word translations and the colour transitions. This was the easiest way to implement animation for both cases. I also saved time by loading the 'words.json' file directly in the activity without using an AsyncTask. My decisions to simplify certain aspects of the game were also motivated in part by the time restriction.

## Improvements

If I had more time I definitely would have loaded the the 'words.json' file in an AsyncTask. Instead of using ValueAnimator I would have liked to implement an animation system that is compatible with older versions of Andriod; the game currently uses ValueAnimator api's added in api 21. There is a small issue in the game where long falling words are cut off on the right; this should be fixed. I also would have liked to add a way to pause or stop the game and also a winning condition. I would have liked to add some logic to display the correct word on the screen if the player did not select any word. More time also would have allowed me to comment my code more thoroughly and to think about the architecture in more detail.
