/**
 **********************************************************************
   Copyright (c) 2017-2018 Arun Kunchithapatham

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   Contributors:
   Arun Kunchithapatham - Initial Contribution
 ***********************************************************************
 *
 */
 
var first_shift_index = null;
var shifts_to_show = 0;
var shifts = null;
var workcenters = null;
var tasplans = null;
var demands = null;

var shiftWidth = 100;
var shiftHeight = 50;
var wrkWidth = 200;
var wrkHeight = 100;
var taskplanHeight = 50;
var millisPerShift = 28800000;
var EPSILON = 0.00001;

var textWidthDiv = null;
var currentReport = null;

function onClickPreviousPage() {
    if (currentReport === "GANTT_CHART") {
        updateGanttChart(-shifts_to_show);
    }
    else if (currentReport === "DEMAND_PLAN") {
        updateDemandChart(-shifts_to_show);
    }
    else {
        console.log("Error! Invalid Report Type");
    }
}

function onClickNextPage() {
    if (currentReport === "GANTT_CHART") {
        updateGanttChart(shifts_to_show);
    }
    else if (currentReport === "DEMAND_PLAN") {
        updateDemandChart(shifts_to_show);
    }
    else {
        console.log("Error! Invalid Report Type");
    }
}

function onClickPreviousShift() {
    if (currentReport === "GANTT_CHART") {
        updateGanttChart(-1);
    }
    else if (currentReport === "DEMAND_PLAN") {
        updateDemandChart(-1);
    }
    else {
        console.log("Error! Invalid Report Type");
    }
}

function onClickNextShift() {
    if (currentReport === "GANTT_CHART") {
        updateGanttChart(1);
    }
    else if (currentReport === "DEMAND_PLAN") {
        updateDemandChart(1);
    }
    else {
        console.log("Error! Invalid Report Type");
    }
}

function onClickGanttChartView() {
    currentReport = "GANTT_CHART";
    clearCharts();
    createGanttChart();
}

function onClickDemandPlanView() {
    currentReport = "DEMAND_PLAN";
    clearCharts();
    createDemandChart();
}

function handleImportInputClickEvent(event) {
    
    let input_file = event.target.files[0];
    let reader = new FileReader();
    reader.onload =
        function(e) {
            let inputStr = e.target.result;
            try {
                // initialize input data related variables
                let input_data = JSON.parse(inputStr);
                shifts = input_data["shifts"];
                workcenters = input_data["workcenters"];
                taskplans = input_data["taskplans"];
                demands = input_data["demands"];
                
                // other initialization
                textWidthDiv = document.getElementById("textWidthDiv");
                
                // initialize default view of gantt chart
                onClickGanttChartView();
                }
            catch (e) {
                console.log(e);
            }
        };
    reader.readAsText(input_file);
}

function clearCharts() {
    
    let gantt_label = document.getElementById("gantt_chart");
    let demand_label = document.getElementById("demand_chart");

    gantt_label.style.backgroundColor = "#D3D3D3";
    demand_label.style.backgroundColor = "#D3D3D3";

    document.body.scrollTop = document.documentElement.scrollTop = 0;
    document.body.scrollLeft = document.documentElement.scrollLeft = 0;

    let header_children = header_row.getElementsByClassName("shift_div");
    while (header_children.length > 0) {
        let last = header_children[header_children.length - 1];
        last.parentNode.removeChild(last);
    }
    
    let content_children = content_area.getElementsByTagName("*");
    while (content_children.length > 0) {
        let last = content_children[content_children.length - 1];
        last.parentNode.removeChild(last);
    }
    
}

function createGanttChart(input_data) {

    let header_row = document.getElementById("header_row");
    let content_area = document.getElementById("content_area");
    let gantt_label = document.getElementById("gantt_chart");
    
    gantt_chart.style.backgroundColor = "#7F9CCB";
    
    viewport_width = document.documentElement.clientWidth;

    content_area.style.width = viewport_width + "px";
    content_area.style.height = wrkHeight * workcenters.length + "px";

    header_row.style.width = viewport_width;
    shifts_to_show = Math.floor((viewport_width - wrkWidth) / shiftWidth) - 1;
    
    first_shift_index = 0;

    for (let i=0; i < workcenters.length; i++) {
        let wrk = workcenters[i];
        
        let wrk_row = document.createElement("div");
        wrk_row.setAttribute("class", "wrk_row");
        wrk_row.setAttribute("id", wrk);
        content_area.appendChild(wrk_row);
        
        let wrk_div = document.createElement("div");
        wrk_div.setAttribute("class", "wrk_div");
        wrk_div.textContent = wrk;
        wrk_row.appendChild(wrk_div);

    }
    
    updateGanttChart(0);
}


function updateGanttChart(offset) {

    let header_row = document.getElementById("header_row");
    let content_area = document.getElementById("content_area");

    let offset_shift_index = first_shift_index + offset;
    // Reached end
    if (offset_shift_index >= shifts.length) {
        return;
    }
    // Not start, but cannot go negative
    else if (offset_shift_index < 0 && first_shift_index > 0) {
        first_shift_index = 0;
    }
    // At start, cannot go negative
    else if (offset_shift_index < 0 && first_shift_index == 0) {
        return;
    }
    // Everything else
    else {
        first_shift_index = offset_shift_index;
    }
    
    // Remove the old shift divs
    let shift_divs = document.getElementsByClassName("shift_div");
    while (shift_divs.length > 0) {
        let last = shift_divs[shift_divs.length - 1];
        last.parentNode.removeChild(last);
    }
    
    // Remove the old taskplan divs
    let taskplan_divs = document.getElementsByClassName("taskplan");
    while (taskplan_divs.length > 0) {
        let last = taskplan_divs[taskplan_divs.length - 1];
        last.parentNode.removeChild(last);
    }
    
    for (let i=first_shift_index; i < Math.min(first_shift_index + shifts_to_show, shifts.length); i++) {
        let shift = shifts[i];
        let shift_div = document.createElement("div");
        shift_div.setAttribute("class", "shift_div");
        shift_div.setAttribute("id", shift["shiftstart"]);
        if (parseFloat(shift["value"]) < EPSILON) {
            shift_div.style.backgroundColor = "red";
        }
        let month_day = shift["shiftstart"].split(" ")[0].split("-");
        shift_div.textContent =  month_day[1] + "-" + month_day[2] + " (" + shift["shiftno"] + ")";
        header_row.appendChild(shift_div);
    }
    
    viewport_start = new Date(shifts[first_shift_index]["shiftstart"]);
    viewport_end = new Date(shifts[Math.min(first_shift_index + shifts_to_show - 1, shifts.length - 1)]["shiftend"]);
    for (let i = 0; i < taskplans.length; i++) {
        let tp = taskplans[i];
        if (tp["workcenterid"] === "null") {
            continue;
        }
        let tp_start = new Date(tp["startdate"]);
        let tp_end = new Date(tp["enddate"]);
        
        if ((tp_start >= viewport_start && tp_start < viewport_end) ||
            (tp_end > viewport_start && tp_end <= viewport_end) ||
            (tp_start < viewport_start && tp_end > viewport_end)) {

            tp_div = document.createElement("div");
            tp_div.setAttribute("class", "taskplan");
            tp_div.title = JSON.stringify(tp, null, '\t');
            tp_div.textContent = tp["skuid"] + "-" + tp["tasknum"];

            let offset = null;
            let tp_len = null;
                    
            if (tp_start >= viewport_start && tp_start < viewport_end) {
                offset = (tp_start - viewport_start)/millisPerShift;
                tp_len = Math.min((tp_end - tp_start), (viewport_end - tp_start)) / millisPerShift;
                if (tp_end > viewport_end) {
                    tp_div.style.borderRight = 'none';
                }
            }
            else if (tp_end > viewport_start && tp_end <= viewport_end) {
                offset = 0;
                tp_len = (tp_end - viewport_start) / millisPerShift;
                tp_div.style.borderLeft = 'none';
            }
            else if (tp_start < viewport_start && tp_end > viewport_end) {
                tp_len = (viewport_end - viewport_start) / millisPerShift;
                tp_div.style.borderLeft = 'none'
                tp_div.style.borderRight = 'none';
            }

            tp_div.style.left = wrkWidth + offset * shiftWidth + "px";
            tp_div.style.width = tp_len * shiftWidth + "px";
            
            let textWidth = getTextWidth(tp["skuid"] + "-" + tp["tasknum"]);
            if (textWidth > 0.8 * tp_len * shiftWidth) {
                tp_div.textContent = "...";
            }

            // Change backgroundColor to gray if this is a released work order
            if (tp["rwo"] !== "null") {
                tp_div.style.backgroundColor = "#d3d3d3";
            }
            
            wrk_row = document.getElementById(tp["workcenterid"]);
            wrk_row.appendChild(tp_div);

        }
    }
}

function createDemandChart(input_data) {

    let header_row = document.getElementById("header_row");
    let content_area = document.getElementById("content_area");
    let demand_label = document.getElementById("demand_chart");
    
    demand_chart.style.backgroundColor = "#7F9CCB";
    
    viewport_width = document.documentElement.clientWidth;

    content_area.style.width = viewport_width + "px";
    content_area.style.height = wrkHeight * workcenters.length + "px";

    header_row.style.width = viewport_width;
    shifts_to_show = Math.floor((viewport_width - wrkWidth) / shiftWidth) - 1;  

    first_shift_index = 0;

    for (let i=0; i < demands.length; i++) {
        let dmd = demands[i];
        
        let dmd_row = document.createElement("div");
        dmd_row.setAttribute("class", "wrk_row");
        dmd_row.setAttribute("id", dmd["demandid"]);
        content_area.appendChild(dmd_row);
        
        let dmd_div = document.createElement("div");
        dmd_div.setAttribute("class", "wrk_div");
        dmd_div.textContent = dmd["demandid"];
        dmd_div.title = JSON.stringify(dmd, null, '\t');
        dmd_row.appendChild(dmd_div);

    }
    
    updateDemandChart(0);
}


function updateDemandChart(offset) {

    let header_row = document.getElementById("header_row");
    let content_area = document.getElementById("content_area");

    let offset_shift_index = first_shift_index + offset;
    // Reached end
    if (offset_shift_index >= shifts.length) {
        return;
    }
    // Not start, but cannot go negative
    else if (offset_shift_index < 0 && first_shift_index > 0) {
        first_shift_index = 0;
    }
    // At start, cannot go negative
    else if (offset_shift_index < 0 && first_shift_index == 0) {
        return;
    }
    // Everything else
    else {
        first_shift_index = offset_shift_index;
    }
    
    // Remove the old shift divs
    let shift_divs = document.getElementsByClassName("shift_div");
    while (shift_divs.length > 0) {
        let last = shift_divs[shift_divs.length - 1];
        last.parentNode.removeChild(last);
    }
    
    // Remove the old taskplan divs
    let taskplan_divs = document.getElementsByClassName("taskplan");
    while (taskplan_divs.length > 0) {
        let last = taskplan_divs[taskplan_divs.length - 1];
        last.parentNode.removeChild(last);
    }
    
    for (let i=first_shift_index; i < Math.min(first_shift_index + shifts_to_show, shifts.length); i++) {
        let shift = shifts[i];
        let shift_div = document.createElement("div");
        shift_div.setAttribute("class", "shift_div");
        shift_div.setAttribute("id", shift["shiftstart"]);
        if (parseFloat(shift["value"]) < EPSILON) {
            shift_div.style.backgroundColor = "red";
        }
        let month_day = shift["shiftstart"].split(" ")[0].split("-");
        shift_div.textContent =  month_day[1] + "-" + month_day[2] + " (" + shift["shiftno"] + ")";
        header_row.appendChild(shift_div);
    }
    
    viewport_start = new Date(shifts[first_shift_index]["shiftstart"]);
    viewport_end = new Date(shifts[Math.min(first_shift_index + shifts_to_show - 1, shifts.length - 1)]["shiftend"]);
    for (let i = 0; i < taskplans.length; i++) {
        let tp = taskplans[i];
        if (tp["demandid"] === "null") {
            continue;
        }
        let tp_start = new Date(tp["startdate"]);
        let tp_end = new Date(tp["enddate"]);
        
        if ((tp_start >= viewport_start && tp_start < viewport_end) || 
            (tp_end > viewport_start && tp_end <= viewport_end) ||
            (tp_start < viewport_start && tp_end > viewport_end)) {

            tp_div = document.createElement("div");
            tp_div.setAttribute("class", "taskplan");
            tp_div.title = JSON.stringify(tp, null, '\t');
            tp_div.textContent = tp["skuid"] + "-" + tp["tasknum"];

            let offset = null;
            let tp_len = null;
                    
            if (tp_start >= viewport_start && tp_start < viewport_end) {
                offset = (tp_start - viewport_start)/millisPerShift;
                tp_len = Math.min((tp_end - tp_start), (viewport_end - tp_start)) / millisPerShift;
                if (tp_end > viewport_end) {
                    tp_div.style.borderRight = 'none';
                }
            }
            else if (tp_end > viewport_start && tp_end <= viewport_end) {
                offset = 0;
                tp_len = (tp_end - viewport_start) / millisPerShift;
                tp_div.style.borderLeft = 'none';
            }
            else if (tp_start < viewport_start && tp_end > viewport_end) {
                tp_len = (viewport_end - viewport_start) / millisPerShift;
                tp_div.style.borderLeft = 'none'
                tp_div.style.borderRight = 'none';
            }

            tp_div.style.left = wrkWidth + offset * shiftWidth + "px";
            tp_div.style.width = tp_len * shiftWidth + "px";
            
            let textWidth = getTextWidth(tp["skuid"] + "-" + tp["tasknum"]);
            if (textWidth > 0.8 * tp_len * shiftWidth) {
                tp_div.textContent = "...";
            }

            // Change backgroundColor to gray if this is a released work order
            if (tp["rwo"] !== "null") {
                tp_div.style.backgroundColor = "#d3d3d3";
            }
            
            dmd_row = document.getElementById(tp["demandid"]);
            dmd_row.appendChild(tp_div);

        }
    }
}

function getTextWidth(text) {
    textWidthDiv.textContent = text;
    return parseInt(window.getComputedStyle(textWidthDiv, null).getPropertyValue('width'), 10);
}
