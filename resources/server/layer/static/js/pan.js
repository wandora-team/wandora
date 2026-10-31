/*
 * 
 * 
 * This code is licensed under the following BSD license:
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy. All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY GRIP STUDIOS INTERACTIVE OY ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL Andrea Leofreddi OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Andrea Leofreddi.
 */

"use strict";

var Pan = {

	// / CONFIGURATION
	// / ====>

	enablePan : 1, // 1 or 0: enable or disable panning (default enabled)
	enableZoom : 1, // 1 or 0: enable or disable zooming (default enabled)
	enableDrag : 0, // 1 or 0: enable or disable dragging (default disabled)

	// / <====
	// / END OF CONFIGURATION

	root : document.documentElement,

	state : 'none',
	svgRoot : null,
	stateTarget : null,
	stateOrigin : null,
	stateTf : null,

	setup : function(id) {
		Pan.svg = document.getElementsByTagName('svg')[0];

		var g = null;
		g = document.getElementById(Pan.canvasID);
		if (g == null)
			g = Pan.svg.getElementsByTagName('g')[0];
		if (g == null)
			alert('Unable to obtain SVG root element');
		Pan.setCTM(g, g.getCTM());
		g.removeAttribute('viewBox');
		Pan.canvas = g;

		Pan.setAttributes(Pan.svg, {
			"onmouseup" : "Pan.handleMouseUp(evt)",
			"onmousedown" : "Pan.handleMouseDown(evt)",
			"onmousemove" : "Pan.handleMouseMove(evt)",
		// "onmouseout" : "Pan.handleMouseUp(evt)", // Decomment this to stop
		// the pan functionality when dragging out of the SVG element
		});

			window.addEventListener('mousewheel', Pan.handleMouseWheel, false); // Chrome/Safari
			window.addEventListener('DOMMouseScroll', Pan.handleMouseWheel, false); // Others
			window.onmouseWheel = document.onmouseWheel = Pan.handleMouseWheel;
	},

	/**
	 * Instance an SVGPoint object with given event coordinates.
	 */
	getEventPoint : function(evt) {
		var p = Pan.svg.createSVGPoint();

		p.x = evt.clientX;
		p.y = evt.clientY;

		return p;
	},

	/**
	 * Sets the current transform matrix of an element.
	 */
	setCTM : function(element, matrix) {
		var s = "matrix(" + matrix.a + "," + matrix.b + "," + matrix.c + ","
				+ matrix.d + "," + matrix.e + "," + matrix.f + ")";
		element.setAttribute("transform", s);
	},

	/**
	 * Dumps a matrix to a string (useful for debug).
	 */
	dumpMatrix : function(matrix) {
		var s = "[ " + matrix.a + ", " + matrix.c + ", " + matrix.e + "\n  "
				+ matrix.b + ", " + matrix.d + ", " + matrix.f
				+ "\n  0, 0, 1 ]";

		return s;
	},

	/**
	 * Sets attributes of an element.
	 */
	setAttributes : function(element, attributes) {
		for ( var i in attributes)
			element.setAttributeNS(null, i, attributes[i]);
	},

	/**
	 * Handle mouse wheel event.
	 */
	handleMouseWheel : function(evt) {

		if (!Pan.enableZoom)
			return;
		var sidebarWidth =  d3.select("#sidebar")[0][0].clientWidth

		// Handle sidebar scrolling
		if (evt.clientX < sidebarWidth) {
			var oldTop = d3.select("#sidebar")[0][0].style.top;
			var delta;

			delta = evt.wheelDelta; 
			if (d3.select("#sidebar")[0][0].clientHeight+parseFloat(oldTop) < window.innerHeight && delta < 0) {
				return;
			}

			var newTop = parseFloat(oldTop)+delta;
			d3.select("#sidebar")[0][0].style.top = 
			(isNaN(newTop) || newTop > 0 || d3.select("#sidebar")[0][0].clientHeight < window.innerHeight) ? 
			0 : newTop + "px";

			return;
		}
			

		if (evt.preventDefault)
			evt.preventDefault();

		evt.returnValue = false;

		var svgDoc = evt.target.ownerDocument;

		var delta;

		if (evt.wheelDelta)
			delta = evt.wheelDelta / 1800; // Chrome/Safari
		else
			delta = evt.detail / -45; // Mozilla

		var z = 1 + delta; // Zoom factor: 0.9/1.1

		var g = Pan.canvas;

		var p = Pan.getEventPoint(evt);

		p = p.matrixTransform(g.getCTM().inverse());

		// Compute new scale matrix in current mouse position
		var k = Pan.svg.createSVGMatrix().translate(p.x, p.y).scale(z).translate(
				-p.x, -p.y);

		Pan.setCTM(g, g.getCTM().multiply(k));

		if (typeof (Pan.stateTf) == "undefined")
			Pan.stateTf = g.getCTM().inverse();

		Pan.stateTf = Pan.stateTf.multiply(k.inverse());
	},

	/**
	 * Handle mouse move event.
	 */
	handleMouseMove : function(evt) {
		if (evt.preventDefault)
			evt.preventDefault();

		evt.returnValue = false;

		if (Pan.state == 'pan' && Pan.enablePan) {
			// Pan mode
			var p = Pan.getEventPoint(evt).matrixTransform(Pan.stateTf);

			Pan.setCTM(Pan.canvas, Pan.stateTf.inverse().translate(p.x - Pan.stateOrigin.x,
					p.y - Pan.stateOrigin.y));
		} else if (Pan.state == 'drag' && enableDrag) {
			// Drag mode
			var p = getEventPoint(evt).matrixTransform(g.getCTM().inverse());

			Pan.setCTM(Pan.stateTarget, Pan.svg.createSVGMatrix().translate(
					p.x - Pan.stateOrigin.x, p.y - Pan.stateOrigin.y).multiply(
					Pan.stateTarger.getCTM().inverse()).multiply(Pan.stateTarget.getCTM()));

			Pan.stateOrigin = p;
		} else {
			if (evt.target.tagName != 'svg') {
				Pan.svg.style.cursor = 'default';
			} else {
				Pan.svg.style.cursor = 'pointer';
			}
		}
	},

	/**
	 * Handle click event.
	 */
	handleMouseDown : function(evt) {

		if (evt.preventDefault)
			evt.preventDefault();

		evt.returnValue = false;

		var tagName = evt.target.tagName;

		if (tagName == "svg" || !Pan.enableDrag // Pan anyway when drag is disabled
		// and the user clicked on an
		// element
		) {
			// Pan mode
			Pan.state = 'pan';
			Pan.stateTf = Pan.canvas.getCTM().inverse();
			Pan.stateOrigin = Pan.getEventPoint(evt).matrixTransform(Pan.stateTf);
		} else {
			// Drag mode
			Pan.state = 'drag';

			Pan.stateTarget = evt.target;

			Pan.stateTf = g.getCTM().inverse();

			Pan.stateOrigin = getEventPoint(evt).matrixTransform(Pan.stateTf);
		}
	},

	/**
	 * Handle mouse button release event.
	 */
	handleMouseUp : function(evt) {
		if (evt.preventDefault)
			evt.preventDefault();

		evt.returnValue = false;

		var svgDoc = evt.target.ownerDocument;

		if (Pan.state == 'pan' || Pan.state == 'drag') {
			// Quit pan mode
			Pan.state = '';
		}
	}
}
