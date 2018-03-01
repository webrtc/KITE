//
//	Copyright 2017 Google Inc.
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	    https://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//
//  Based on the sequence sunburst example
//  at https://gist.github.com/kerryrodden/7090426 - Apache 2.0 license

function drawCircle(myData){
    var height = width,
      radius = (Math.min(width, height) / 2) - 10;
    // Breadcrumb dimensions: width, height, spacing, width of tip/tail.
    var b = {
      w: 100,
      h: 20,
      s: 3,
      t: 10
    };
    var formatNumber = d3.format(",d");
    var x = d3.scale.linear()
      .range([0, 2 * Math.PI]);
    var y = d3.scale.linear()
        .range([0, radius]);
    var color = d3.scale.category20c();
    var labelVsNodes = [];
    var currentDepth=1;
    var currentNode = [];
    var items = myData.children;
    var allItems = [];
    var homeNode;
    var layerCount = 0;
    var delta = 0;

    for (i = 0; i < items.length; i++){
        allItems.push(items[i].name);
    }
    var maxTextLegendWidth = 0;
    for (i = 0; i < allItems.length; i++) {
      maxTextLegendWidth = Math.max(maxTextLegendWidth, getTextWidth(allItems[i], "14pt sans-serif"));
    }

    b.w = maxTextLegendWidth;



    function computeTextRotation(d) {
      return (x(d.x + d.dx / 2) - Math.PI / 2) / Math.PI * 180;
    }
    var partition = d3.layout.partition()
      .value(function(d) {
        return d.size;
      });

    var arc = d3.svg.arc()
        .startAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })
        .endAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })
        .innerRadius(function(d) {
            layerCount = 1/d.dy;
            delta = 0.85/(layerCount-2);
            if(d.depth == layerCount - 1)
                return y(0.95);
            if (d.depth > 0)
                return y(0.1 + (d.depth-1)*delta);
            return 0.05; })
        .outerRadius(function(d) {
            layerCount = 1/d.dy;
            delta = 0.83/(layerCount-2);
            if(d.depth == layerCount - 1)
                return y(1);
            if (d.depth > 0)
                return y(0.1 + (d.depth)*delta);
            return 0.1;
        });

    var svg = d3.select("#chart").append("svg")
      .attr("width", width)
      .attr("height", height)
      .attr("id", "container") // added
      .append("g")
      .attr("transform", "translate(" + width / 2 + "," + (height / 2) + ")");


    // Add the mouseleave handler to the bounding circle.
    d3.select("#container").on("mouseleave", mouseleave); // added

    // Basic setup of page elements.
    drawLegend(items);
    initializeBreadcrumbTrail();


    var g = svg.selectAll("g")
      .data(partition.nodes(myData))
      .enter().append("g")
      .style("stroke", function(d) {
           if(d.name=="OK"||d.name=="FAILED"||d.name=="PENDING"||d.name=="ERROR")
               return "#000";
           return "#fff";
           });
    var path = g.append("path")
      .attr("id",function(d) {
            return d.name+d.depth;
      })
      .attr("d", arc)
        .style("fill", function(d) {
            if(d.name=="OK")
                return "#42f4aa";
            if(d.name=="FAILED")
                return "#ea464e";
            if(d.name=="PENDING")
                return "#f4ce42";
            if(d.name=="ERROR")
            return "#3366ff";
            if(d.name=="result"){
                currentNode["result0"]=d;
                homeNode = d;
                return "#000000";
            }
            return color(d.name);
        })
      .on("click", click)
      .on("mouseover", mouseover);

    var text = g.append("text")
        .attr("transform", function(d) {
            return "rotate(" + computeTextRotation(d) + ")";
            })
        .attr("x", function(d) {
            if(d.depth==0)
                return -2;
            return y(0.13+(d.depth-1)*delta); })
        .attr("dx", "7") // margin
        .attr("dy", ".35em")
        .attr("style", function (d){
            return "font-size:8px;cursor:pointer;";
        })// vertical-align
        .text(function(d) {
        if(d.depth==0)
            return "";
        if(d.depth==currentDepth-1)
            return "";
        if(d.depth<=currentDepth)
            return d.name;
        })
        .on("click", click);



    function click(d) {
        // fade out all text elements
        var sequenceArray = getAncestors(d);
        updateOldLayers(sequenceArray);

        addCurrent(d);
        drawLegend(d.children);
        text.transition().attr("opacity", 0);
        currentDepth=d.depth+1;
        path.transition()
        .duration(600)
        .attrTween("d", arcTween(d))
        .each("end", function(e, i) {
            // check if the animated element's data e lies within the visible angle span given in d
            if (e.x>= d.x && e.x < (d.x + d.dx)||e.depth==currentDepth-2) {
            // get a selection of the associated text element
            var arcText = d3.select(this.parentNode).select("text");
            // fade in the text element and recalculate positions
            arcText.transition().duration(650)
            .attr("opacity", 1)
            .attr("transform", function() { return "rotate(" + computeTextRotation(e) + ")" })
            .attr("x", function(d) {
                if(d.depth==currentDepth-2)
                    return -5;
                return y(0.12+(d.depth-1)*delta); })
            .text(function(d) {
            if(d.depth==currentDepth-2)
                return "";
            if(d.depth<=currentDepth)
                return d.name;
            return"";});
            }
        });


    }

    function arcTween(d) {
      var xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
          yd = d3.interpolate(y.domain(), [d.y, 1]),
          yr = d3.interpolate(y.range(), [d.y ? 20 : 0, radius]);
      return function(d, i) {
        return i
            ? function(t) { return arc(d); }
            : function(t) { x.domain(xd(t)); y.domain(yd(t)).range(yr(t)); return arc(d); };
      };
    }

    // Fade all but the current sequence, and show it in the breadcrumb trail.
    function mouseover(d) {

      var sequenceArray = getAncestors(d);
      updateBreadcrumbs(sequenceArray);

      // Fade all the segments.
      d3.selectAll("g")
        .style("opacity", 1);

      // Then highlight only those that are an ancestor of the current segment.
      svg.selectAll("g")
        .filter(function(node) {
          return (sequenceArray.indexOf(node) >= 0);
        })
        .style("opacity", 1);
    }

    // Given a node in a partition layout, return an array of all of its ancestor
    // nodes, highest first, but excluding the root.
    function getAncestors(node) {
      var path = [];
      var current = node;
      while (current.parent) {
        path.unshift(current);
        current = current.parent;
      }
      return path;
    }

    // Restore everything to full opacity when moving off the visualization.
    function mouseleave(d) {

      // Hide the breadcrumb trail
      d3.select("#trail")
        .style("visibility", "hidden");

      // Deactivate all segments during transition.
      //d3.selectAll("path").on("mouseover", null);

      // Transition each segment to full opacity and then reactivate it.
      d3.selectAll("g")

        .style("opacity", 1)

    }


    function initializeBreadcrumbTrail() {
      // Add the svg area.
      var trail = d3.select("#sequence").append("svg:svg")
        .attr("width", width*1.2)
        .attr("height", 30)
        .attr("id", "trail");
      var trail = d3.select("#oldLayer").append("svg:svg")
        .attr("width", width)
        .attr("height", 30)
        .attr("id", "fixTrail");
    }

    // Generate a string that describes the points of a breadcrumb polygon.
    function breadcrumbPoints(d, i) {
      var points = [];
      var widthForThisLabel = b.w/1.5;

      points.push("0,0");
      points.push(widthForThisLabel + ",0");
      points.push(widthForThisLabel + b.t + "," + (b.h / 2));
      points.push(widthForThisLabel + "," + b.h);
      points.push("0," + b.h);
      if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
        points.push(b.t + "," + (b.h / 2));
      }
      return points.join(" ");
    }


    function drawLegend(dota) {
      var data = getChildren(dota)
      // Dimensions of legend item: height, spacing, radius of rounded rect. width will be set dynamically
      var li = {
        w: 150,
        h: 25,
        s: 5,
        r: 3
      };
      $( ".legend" ).remove();
      var legend = d3.select("#legend").append("svg:svg")
        .attr("class", "legend")
        .attr("width", li.w)
        .attr("height", d3.keys(data).length * (li.h + li.s));
      var heightDiv = ""+(d3.keys(data).length * (li.h + li.s)+100);
      $("#overviewDiv").css("height",heightDiv);
      var labelVsColors = [];
      labelVsNodes = [];

      for (i = 0; i < data.length; i++) {
        labelVsColors[data[i]] = color(data[i]);
        labelVsNodes[data[i]] = dota[i];
      }
      var g = legend.selectAll("g")
        .data(d3.entries(labelVsColors))
        .enter().append("svg:g")
        .attr("transform", function(d, i) {
          return "translate(0," + i * (li.h + li.s) + ")";
        });

      g.append("svg:rect")
        .attr("rx", li.r)
        .attr("ry", li.r)
        .attr("name", function(d) {
          return d.key+currentDepth;
        })
        .attr("width", li.w)
        .attr("height", li.h)
        .style("fill", function(d) {
            return d.value;
        }).on("click", function(d){
            click(labelVsNodes[d.key]);
        });

      g.append("svg:text")
        .attr("x", li.w / 2)
        .attr("y", li.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "middle")
        .attr("style", "font-size:12px;cursor:pointer;color:black;")
        .style("pointer-events", "none")
        .text(function(d) {
          return d.key;
        });
    }



    // Update the breadcrumb trail to show the current sequence and percentage.
    function updateBreadcrumbs(nodeArray) {

      // Data join; key function combines name and depth (= position in sequence).
      var g = d3.select("#trail")
        .selectAll("g")
        .data(nodeArray, function(d) {
          return d.name + d.depth;
        });

      // Add breadcrumb and label for entering nodes.
      var entering = g.enter().append("svg:g");

      entering.append("svg:polygon")
        .attr("points", breadcrumbPoints)
        .style("fill", function(d) {
        if(d.name=="OK")
            return "#42f4aa";
        if(d.name=="FAILED")
            return "#ea464e";
        if(d.name=="PENDING")
            return "#f4ce42";
        if(d.name=="ERROR")
            return "#3366ff";
        return color((d.children ? d : d.parent).name);
        });


      entering.append("svg:text")
        .attr("x", (b.w + b.t) / 3)
        .attr("y", b.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "middle")
        .attr("style", "font-size:10px;cursor:pointer;color:black;")
        .text(function(d) {
          return d.name;
        });

      // Set position for entering and updating nodes.
      g.attr("transform", function(d, i) {
        return "translate(" + i * (b.w/1.5 + b.s) + ", 0)";
      });

      // Remove exiting nodes.
      g.exit().remove();


      // Make the breadcrumb trail visible, if it's hidden.
      d3.select("#trail")
        .style("visibility", "");

    }

    // Update the breadcrumb trail to show the current sequence and percentage.
    function updateOldLayers(nodeArray) {
      nodeArray.splice(0,0,homeNode);
      // Data join; key function combines name and depth (= position in sequence).
      var g = d3.select("#fixTrail")
        .selectAll("g")
        .data(nodeArray, function(d) {
          return d.name + d.depth;
        });

      // Add breadcrumb and label for entering nodes.
      var entering = g.enter().append("svg:g");

      entering.append("svg:polygon")
        .attr("points", breadcrumbPoints)
        .style("fill", function(d) {
        if(d.name=="OK")
            return "#42f4aa";
        if(d.name=="FAILED")
            return "#ea464e";
        if(d.name=="PENDING")
            return "#f4ce42";
        if(d.name=="ERROR")
            return "#3366ff";
        if(d.name=="result")
            return "#adadad";
        return color((d.children ? d : d.parent).name);
        }).on("click", function(d){
            if(d.parent)
                click(currentNode[d.parent.name+d.parent.depth]);
            if(d.name=='result')
                click(currentNode['result0']);
        });


      entering.append("svg:text")
        .attr("x", (b.w + b.t) / 3)
        .attr("y", b.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "middle")
        .attr("style", "font-size:10px;cursor:pointer;color:black;")
        .text(function(d) {
            if(d.name=="result")
                return "HOME";
            return d.name;
        }).on("click", function(d){
            if(d.parent)
                click(currentNode[d.parent.name+d.parent.depth]);
            if(d.name=='result')
                click(currentNode['result0']);
        });

      // Set position for entering and updating nodes.
      g.attr("transform", function(d, i) {
        return "translate(" + i * (b.w/1.5 + b.s) + ", 0)";
      });

      // Remove exiting nodes.
      g.exit().remove();


      // Make the breadcrumb trail visible, if it's hidden.
      d3.select("#fixTrail")
        .style("visibility", "");

    }

    d3.select(self.frameElement).style("height", height + "px");

    function getTextWidth(text, font) {
      // re-use canvas object for better performance
      var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
      var context = canvas.getContext("2d");
      context.font = font;
      var metrics = context.measureText(text);
      return metrics.width;
    };

    function flatten(root) {
      var nodes = [],
        i = 0;

      function recurse(node) {
        if (node.children)
            node.children.forEach(recurse);
        if (!node.id)
            node.id = ++i;
        nodes.push(node);
      }

      recurse(root);
      return nodes;
    }


    function getChildren(data) {
        var children = [];
        for(i=0;i<data.length;i++){
            children.push(data[i].name);
        }
        return children;
    }

    function addCurrent(node){
        currentNode[node.name+node.depth]=node;
        if (node.parent){
            currentNode[node.parent.name+node.parent.depth] = node.parent;
            addCurrent(node.parent)
        }
    }

}

