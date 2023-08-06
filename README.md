# NailStringArt
(Grayscale Src Image, Nail Count, Computation Resolution) -> (Image "Drawn" w/ String, Nail Thread Order)

# Configuring Weights
In the bottom of the Java program, there are numerous unlabelled boxes. These represent the following algorithm weights:
1. Transition Underdrawn Score: This is a value representing when the current pixel value + the string's "influence" supercedes the target pixel value is. Only counts curValue -> targetValue (ignores overshoot).
2. Transition Overdrawn Score: This is the counterpart to #1. This represents the overshoot itself.
3. Overdrawing Pitch Black Score: This represents pixels where you are drawing over pitch black (#000000) pixels specifically.
4. Complete Overdrawing Score: This represents pixels where the current pixel value ALREADY exceeds the target pixel value, and yet we are adding more.
5. Avoid Encountered Score: Similar to pitch black, but specifically targets grayscale value 20 (#141414). Can be used to specifically create "avoid" zones in input images.
6. Raw Positive Influence Score: This is similar to transition underdrawn, but this conceptually represents how MORE a brighter pixel needs to be filled in than a duller one. #1 treats all pixels which need to be filled in equally weighed, but this takes into consideration priority for pixels which need to be filled in MORE.
7. Raw Negative Influence Score: This is analogous to #6 but the negativity (overdrawing) counterpart.

# Important Technological Note
I've seen other nail drawing techniques online for a spectrum of "free"ness. This is my contribution. It is unique from the others for the following significant reasons:
- It allows a compute resolution surpassing ANYTHING the others have. I've seen the main competitor on GitHub renders in a 500x500 max computation space. My software can handle 1000x1000 are about 1:1 speed and the precompute solution to gauge string "influence" (should it be drawn) only takes up ~1 GB for that.
- It allows tweaking the "optimal next string" algorithms parameters. Others bake them in for simplicity sake I'd assume to something that "works best" in a shotgun spread average kind of way, but mine allows you to really get nichely calibrated products depending on your input picture.

![Example Product](https://i.imgur.com/7pzatRv.png)
