# Lib_LPD8
PureData and SuperCollider libraries for using the AKAI LPD8.

## For SuperCollider
To install, dump the LPD8.sc file into your SC Extensions directory. You can find out where this is by running the following line in SC:


`Platform.userExtensionDir`


See this link for more info

[Extending SuperCollider](http://danielnouri.org/docs/SuperColliderHelp/Extending%20and%20Customizing%20SC/Using-Extensions.html)


## Usage (SuperCollider)
Very simple for now & needs a lot of work to take advantage of all the settings the LPD8 offers. For now, put it on program 3 and pad mode.

__USAGE:__
```
l = LPD8.new // create a new LPD8

// Add responder functions to different controls with addAction
l.addAction('K1', {|val| val.postln});

// And if no LPD8 is available, open a virtual interface
l.gui;
```

__LPD8 Control Codes:__

PADS:
P5
P6
P7
P8
P1
P2
P3
P4

KNOBS:
K1
K2
K3
K4
K5
K6
K7
K8
