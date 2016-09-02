/*
MIDI Control Class for the LPD8

Very simple for now & need a lot of work to take advantage of all the settings.
Put it on program 3 and pad mode to use.

USAGE:
l = LPD8.new
l.addAction('K1', {|val| val.postln});

And if no LPD8 is available...
l.gui; // open up a virtual LPD8

LPD8 Midi Codes:

PROGRAM 1 / PAD MODE - Pads are Toggles

PADS (CH 0 - note on has velocity, note off has velocity 127)
P5 - Note 60
P6 - Note 62
P7 - Note 64
P8 - Note 67
P1 - Note 69
P2 - Note 71
P3 - Note 72
P4 - Note 74

KNOBS (CH 0)
K1 - ctrl 1
K2 - ctrl 2
K3 - ctrl 3
K4 - ctrl 4
K5 - ctrl 5
K6 - ctrl 6
K7 - ctrl 7
K8 - ctrl 8


PROGRAM 2 / PAD MODE - Pads are Triggers

PADS (CH 1 - note on has velocity, note off has velocity 127)
P5 - Note 37
P6 - Note 38
P7 - Note 46
P8 - Note 44
P1 - Note 35
P2 - Note 36
P3 - Note 42
P4 - Note 39

KNOBS (CH 1)
K1 - ctrl 1
K2 - ctrl 2
K3 - ctrl 3
K4 - ctrl 4
K5 - ctrl 5
K6 - ctrl 6
K7 - ctrl 7
K8 - ctrl 8


PROGRAM 3 / PAD MODE - Pads are Triggers

PADS (CH 2 - note on has velocity, note off has velocity 127)
P5 - Note 67
P6 - Note 69
P7 - Note 71
P8 - Note 72
P1 - Note 60
P2 - Note 62
P3 - Note 64
P4 - Note 65

KNOBS (CH 2)
K1 - ctrl 1
K2 - ctrl 2
K3 - ctrl 3
K4 - ctrl 4
K5 - ctrl 5
K6 - ctrl 6
K7 - ctrl 7
K8 - ctrl 8


PROGRAM 4 / PAD MODE - Pads are Triggers

PADS (CH 3 - note on has velocity, note off has velocity 127)
P5 - Note 43
P6 - Note 45
P7 - Note 47
P8 - Note 48
P1 - Note 36
P2 - Note 38
P3 - Note 40
P4 - Note 41

KNOBS (CH 3)
K1 - ctrl 1
K2 - ctrl 2
K3 - ctrl 3
K4 - ctrl 4
K5 - ctrl 5
K6 - ctrl 6
K7 - ctrl 7
K8 - ctrl 8

*/

LPD8 {
	classvar pads_to_midinotes, knobs_to_cc;
	var note_actions, cc_actions;
	var midifunc_noteon, midifunc_noteoff, midifunc_cc;
	var gui_win, gui_knobs, gui_pads;

	*initClass {
		pads_to_midinotes = [
			('P1': 69, 'P2': 71, 'P3': 72, 'P4': 74, 'P5': 60, 'P6': 62, 'P7': 64, 'P8': 67), // CH0 - Program 1
			('P1': 35, 'P2': 36, 'P3': 42, 'P4': 39, 'P5': 37, 'P6': 38, 'P7': 46, 'P8': 44), // CH1 - Program 2
			('P1': 60, 'P2': 62, 'P3': 64, 'P4': 65, 'P5': 67, 'P6': 69, 'P7': 71, 'P8': 72), // CH2 - Program 3
			('P1': 36, 'P2': 38, 'P3': 40, 'P4': 41, 'P5': 43, 'P6': 45, 'P7': 47, 'P8': 48)];// CH3 - Program 4
		knobs_to_cc = [
			('K1': 1, 'K2': 2, 'K3': 3, 'K4': 4, 'K5': 5, 'K6': 6, 'K7': 7, 'K8': 8),
			('K1': 1, 'K2': 2, 'K3': 3, 'K4': 4, 'K5': 5, 'K6': 6, 'K7': 7, 'K8': 8),
			('K1': 1, 'K2': 2, 'K3': 3, 'K4': 4, 'K5': 5, 'K6': 6, 'K7': 7, 'K8': 8),
			('K1': 1, 'K2': 2, 'K3': 3, 'K4': 4, 'K5': 5, 'K6': 6, 'K7': 7, 'K8': 8)];
	}

	*new {|cmdperiod=true|
		^super.new.init(cmdperiod);
	}

	init {|cmdperiod|
		note_actions = [(),(),(),()];
		cc_actions = [(),(),(),()];
		{
		if(MIDIClient.sources.isNil) {
			"Initializing MIDI Subsystem...".postln;
			MIDIClient.init;
				2.wait;
			"Connecting MIDI Devices...".postln;
			MIDIIn.connectAll;
				2.wait;
			"Connecting MIDI Devices Successful...".postln;
		} {
			"MIDI Devices Already Initialized...".postln;
		};

		"Setting up MIDI responders...".postln;
		midifunc_noteon = MIDIFunc.noteOn({|vel, note, chan|
			var action = note_actions[chan][note];
			if(action.notNil) {
				action.value(vel, 'ON');
			} {
				Post << "MIDI NOTE ON " << note << " Velocity:" << vel << " Chan:" << chan << $\n;
			};
			},(30..80), (0..3));
		midifunc_noteoff = MIDIFunc.noteOff({|vel, note, chan|
			var action = note_actions[chan][note];
			if(action.notNil) {
				action.value(vel, 'OFF');
			} {
				Post << "MIDI NOTE OFF " << note << " Velocity:" << vel << " Chan:" << chan << $\n;
			};
			},(30..80), (0..3));
		midifunc_cc = MIDIFunc.cc({|val, cc, chan|
			var action = cc_actions[chan][cc];
			if(action.notNil) {
				action.value(val);
			} {
				Post << "MIDI CC " << cc << " Value:" << val << " Chan:" << chan << $\n;
			};
			}, (1..8), (0..3));
	}.fork;
		if(cmdperiod == true) {
			CmdPeriod.add({this.close;});
		};
	}

	/**************
	Add a function to respond to a specific control widget & channel (Program1 sends to channel 0)
	Widget symbols are - 'K1' ... 'K8' for knobs, 'P1' ... 'P8' for Pads
	*************/
	addAction {|widget, func, midichan=0|
		var noteorcc;
		noteorcc = pads_to_midinotes[midichan][widget];
		if(noteorcc.notNil) {
			// It's a pad
			note_actions[midichan].put(noteorcc, func);
		} {
			noteorcc = knobs_to_cc[midichan][widget];
			if(noteorcc.notNil) {
				// It's a knob
				cc_actions[midichan].put(noteorcc, func);
			} {
				Post << "Invalid Widget Code " << widget << " has no matching Note or CC value." << $\n;
			};
		};
	}

	close {
		"Removing MIDI functions for LPD8...".postln;
		midifunc_noteon.free;
		midifunc_noteoff.free;
		midifunc_cc.free;
		this.release;
	}


	gui {
			var chan = 0;
		if(gui_win.isNil) {
			gui_knobs = ();
			gui_pads = ();
			gui_win = Window.new("LPD8", Rect(0,0, 870, 260));
			[['P5','P6','P7','P8'],['P1','P2','P3','P4']].do {|row, rownum|
				row.do {|sym, colnum|
					var pad = Button(gui_win, Rect((colnum * 110) + 20, 20 + (rownum * 120), 100, 100));
					pad.states = [[sym, Color.white, Color.black],[sym, Color.white, Color.new255(200, 140, 0)]];
					pad.action = {|but|
						var val, note, action;
						val = but.value;
						if(val == 1) {
							val = 'ON';
						} {
							val = 'OFF';
						};
						note = pads_to_midinotes[chan][sym];
						action = note_actions[chan][note];
						if(action.notNil) {
							action.value(127, val);
						} {
							Post << "MIDI NOTE " << val << " " << note << " " << 127 << $\n;
						};
					};
					gui_pads.put(sym, pad);
				};
			};
		};
		[['K1', 'K2', 'K3', 'K4'],['K5', 'K6', 'K7', 'K8']].do {|row, rownum|
			row.do {|sym, colnum|
				var knob = EZKnob(gui_win, Rect(480 + (colnum * 100), 20 + (rownum * 120), 60, 100), sym, ControlSpec(0, 127, 'lin', 1));
				knob.action = {|kn|
					var ccval, action, value;
					value = kn.value;
					ccval = knobs_to_cc[chan][sym];
					action = cc_actions[chan][ccval];
					if(action.notNil) {
						action.value(value);
					} {
						Post << "MIDI CC " << ccval << " " << value << $\n;
					};
				};
				gui_knobs.put(sym, knob);
			};
		};
		gui_win.front;
		Window

	}
}