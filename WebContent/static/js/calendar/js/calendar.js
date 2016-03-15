// Visual Studio references

/// <reference path="jquery-1.9.1.min.js" />
/// <reference path="jquery-ui-1.10.2.min.js" />
/// <reference path="moment.min.js" />
/// <reference path="timelineScheduler.js" />

var today = moment().startOf('day');

var Calendar = {
    Periods: [

        {
            Name: '1 week',
            Label: '1 week',
            TimeframePeriod: (60 * 24),
            TimeframeOverall: (60 * 24 * 7),
            TimeframeHeaders: [
                'MMM',
                'Do'
            ],
            Classes: 'period-1week'
        },
        {
            Name: '1 month',
            Label: '1 month',
            TimeframePeriod: (60 * 24 * 1),
            TimeframeOverall: (60 * 24 * 28),
            TimeframeHeaders: [
                'MMM',
                'Do'
            ],
            Classes: 'period-1month'
        }
    ],

    Items: [
        {
            id: 202,
            name: '<div>Erick Quitos</div><div>Info:...</div>',
            sectionID: 1,
            start: moment(today).add('days', -1),
            end: moment(today).add('days', 3),
            classes: 'item-status',
            events: [
            ]
        },
        {
            id: 212,
            name: '<div>Esly Mayrena</div><div>Info: Gay...</div>',
            sectionID: 3,
            start: moment(today).add('days', 1),
            end: moment(today).add('days', 4),
            classes: 'item-status',
            events: [
            ]
        }
    ],

    Sections: [
        {
            id: 1,
            name: 'Room 1'
        },
        {
            id: 2,
            name: 'Room 2'
        },
        {
            id: 3,
            name: 'Room 3'
        },
		{
            id: 4,
            name: 'Room 4'
        },
        {
            id: 5,
            name: 'Room 5'
        },
        {
            id: 6,
            name: 'Room 6'
        },
		{
            id: 7,
            name: 'Room 1'
        },
        {
            id: 8,
            name: 'Room 8'
        },
        {
            id: 9,
            name: 'Room 9'
        },
		{
            id: 10,
            name: 'Room 10'
        },
        {
            id: 11,
            name: 'Room 11'
        },
        {
            id: 12,
            name: 'Room 12'
        },
		{
            id: 13,
            name: 'Room 13'
        },
        {
            id: 14,
            name: 'Room 14'
        },
        {
            id: 15,
            name: 'Room 15'
        },
		{
            id: 16,
            name: 'Room 16'
        },
        {
            id: 17,
            name: 'Room 17'
        },
        {
            id: 18,
            name: 'Room 18'
        },
		{
            id: 19,
            name: 'Room 19'
        },
		{
            id: 20,
            name: 'Room 20'
        },
        {
            id: 21,
            name: 'Room 21'
        },
        {
            id: 22,
            name: 'Room 22'
        },
		{
            id: 23,
            name: 'Room 23'
        },
        {
            id: 24,
            name: 'Room 24'
        },
        {
            id: 25,
            name: 'Room 25'
        },
		{
            id: 26,
            name: 'Room 26'
        },
        {
            id: 27,
            name: 'Room 27'
        },
        {
            id: 28,
            name: 'Room 28'
        },
		{
            id: 29,
            name: 'Room 29'
        },
        {
            id: 30,
            name: 'Room 30'
        },
        {
            id: 31,
            name: 'Room 31'
        },
		{
            id: 32,
            name: 'Room 32'
        },
        {
            id: 33,
            name: 'Room 33'
        },
        {
            id: 34,
            name: 'Room 34'
        }
    ],

    Init: function () {
        TimeScheduler.Options.GetSections = Calendar.GetSections;
        TimeScheduler.Options.GetSchedule = Calendar.GetSchedule;
        TimeScheduler.Options.Start = today;
        TimeScheduler.Options.Periods = Calendar.Periods;
        TimeScheduler.Options.SelectedPeriod = '1 month';
        TimeScheduler.Options.Element = $('.calendar');

        TimeScheduler.Options.AllowDragging = false;
        TimeScheduler.Options.AllowResizing = false;

        TimeScheduler.Options.Events.ItemClicked = Calendar.Item_Clicked;
        TimeScheduler.Options.Events.ItemDropped = Calendar.Item_Dragged;
        TimeScheduler.Options.Events.ItemResized = Calendar.Item_Resized;

        //TimeScheduler.Options.Events.ItemMovement = Calendar.Item_Movement;
        //TimeScheduler.Options.Events.ItemMovementStart = Calendar.Item_MovementStart;
        //TimeScheduler.Options.Events.ItemMovementEnd = Calendar.Item_MovementEnd;

        TimeScheduler.Options.Text.NextButton = '&nbsp;';
        TimeScheduler.Options.Text.PrevButton = '&nbsp;';

        TimeScheduler.Options.MaxHeight = 100;

        TimeScheduler.Init();
    },

    GetSections: function (callback) {
        callback(Calendar.Sections);
    },

    GetSchedule: function (callback, start, end) {
        callback(Calendar.Items);
    },

    Item_Clicked: function (item) {
        console.log(item);
    },

    Item_Dragged: function (item, sectionID, start, end) {
        var foundItem;

        console.log(item);
        console.log(sectionID);
        console.log(start);
        console.log(end);

        for (var i = 0; i < Calendar.Items.length; i++) {
            foundItem = Calendar.Items[i];

            if (foundItem.id === item.id) {
                foundItem.sectionID = sectionID;
                foundItem.start = start;
                foundItem.end = end;

                Calendar.Items[i] = foundItem;
            }
        }

        TimeScheduler.Init();
    },

    Item_Resized: function (item, start, end) {
        var foundItem;

        console.log(item);
        console.log(start);
        console.log(end);

        for (var i = 0; i < Calendar.Items.length; i++) {
            foundItem = Calendar.Items[i];

            if (foundItem.id === item.id) {
                foundItem.start = start;
                foundItem.end = end;

                Calendar.Items[i] = foundItem;
            }
        }

        TimeScheduler.Init();
    },

    Item_Movement: function (item, start, end) {
        var html;

        html =  '<div>';
        html += '   <div>';
        html += '       Start: ' + start.format('Do MMM YYYY HH:mm');
        html += '   </div>';
        html += '   <div>';
        html += '       End: ' + end.format('Do MMM YYYY HH:mm');
        html += '   </div>';
        html += '</div>';

        $('.realtime-info').empty().append(html);
    },

    Item_MovementStart: function () {
        $('.realtime-info').show();
    },

    Item_MovementEnd: function () {
        $('.realtime-info').hide();
    }
};

$(document).ready(Calendar.Init);