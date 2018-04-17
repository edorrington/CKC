Highcharts.setOptions({
    credits: { enabled: false },
    exporting: { url: "http://ckc-export.noeticlogic.com/highcharts-export/" },
    chart: {
        backgroundColor: "rgba(255,255,255,0)",
        style: {
            color: '#333',
            fontFamily: 'sans-serif'
        }
    },
    title: {
        style: {
            color: '#a3a3a3',
            font: 'bold 18px sans-serif'
        }
    },
    subtitle: {
        style: {
            color: '#333',
            font: 'bold 12px sans-serif'
        }
    },

    legend: {
        itemStyle: {
            font: '9pt sans-serif',
            color: '#333'
        },
    }
});

Charts = {
    pieChart: function pieChart(container, title, subtitle, sname, data, url, filename) {
		var elem = $("#"+container),
			width = elem.width(),
			height = elem.height();
    	if (!data || data.length == 0) {
    		//$("#"+container).html('<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="'+width+'" height="'+height+'"><text x="'+(width/2)+'" y="25" style="font:bold 18px sans-serif;fill:#a3a3a3;" text-anchor="middle" zIndex="4"><tspan x="'+(width/2)+'">Languages</tspan></text><circle cx="'+(width/2)+'" cy="'+(height/2+25)+'" r="'+Math.round(Math.min(width,height)/3)+'" style="stroke-width:0" fill="#ededed"></circle><text x="'+(width/2)+'" y="'+(height/2+32)+'" style="color:#a3a3a3;font:bold 18px sans-serif;fill:#ffffff;" text-anchor="middle" zIndex="5"><tspan x="'+(width/2)+'">None</tspan></text></svg>');
    	}
        filename = typeof filename !== 'undefined' ? filename : 'chart';
        data = data.filter(function(d) { return d.y > 0; });
        var chart = new Highcharts.Chart({
            chart: { renderTo: container, type: 'pie', width: width, height: height },
            title: { text: (url ? '<a href="'+url+'">'+title+'</a>' : title), useHTML: url ? true : false },
            subtitle: { text: subtitle },
			tooltip: { formatter: function() { return '<b>'+ this.point.name.replace(/</g, "&lt;") +'</b>: '+ this.point.o + '/' + this.point.e + ' (' + Math.round(10*this.percentage)/10 +'%)';}, percentageDecimals: 1 },
			plotOptions: {
				pie: {
					allowPointSelect: true,
					cursor: 'pointer',
					dataLabels: {
						enabled: true,
						color: '#000000',
						connectorColor: '#000000',
						useHTML: false,
						distance: 15,
						formatter: function() { return '<b>'+ this.point.name.replace(/</g, "&lt;") +'</b>';} },
					series: { animation: { duration: 300 } }
				}
			},
            series: [{ name: sname, data: data }],
			exporting: { scale: 3, filename: filename, enabled: true }
        });
        return {
            refresh: function(data) {
                chart.series[0].setData(data);
            },
            c: chart
        };
    },

    barChart: function barChart(container, opts) {
		var elem = $("#"+container),
			width = elem.width(),
			height = elem.height();
        var mopts = Highcharts.merge({title:"",subtitle:"",filename:"chart",highlightLast:false},opts);
		var chart = new Highcharts.Chart({
			chart: { renderTo: container, type: 'bar', width: width, height: height, zoomType: 'x', panning: true, panKey: 'shift' },
            title: { text: (mopts.url ? '<a href="'+mopts.url+'">'+mopts.title+'</a>' : mopts.title), useHTML: mopts.url ? true : false },
            subtitle: { text: mopts.subtitle },
            xAxis: {
                categories: mopts.data.map(function(d) { return d.name}),
                labels: {
                    formatter: function() {
                        var ML = 18;
                        var vt = this.value.toString().trim();
                        var vl = vt.length;
                        var idx = Math.min(ML,vl);
                        var lbl = vt.slice(0,idx) + (idx < vl ? '…' : '');
                        return (mopts.highlightLast && this.isLast) ? '<strong>'+lbl+'</strong>' : lbl;
                    },
                    useHTML: false
                },
                minRange: 1
            },
            yAxis: { title: null, labels: { enabled: false } },
            legend: { shadow: false },
//			legend: { layout: 'vertical', floating: true, backgroundColor: '#FFFFFF', align: 'right', verticalAlign: 'top', y: 60, x: -60, shaodw: false },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.x + '</b><br/>' +
                        this.series.name + ': ' + this.y +
                        (mopts.ttitle ? '<br/>' +
                        mopts.ttitle+': ' + this.point.stackTotal : '');
                }
            },
            exporting: { scale: 3, filename: mopts.filename, enabled: true },
            series: mopts.categories.map(function(d,idx) { return {name: d, data: mopts.data.map(function(e) { return e.data[idx]; })}; }),
            plotOptions: { bar: { stacking: 'normal' }, series: { animation: { duration: 0 } } }
		});
		return chart;
	},

    colChart: function colChart(container, opts) {
        var elem = $("#"+container),
            width = elem.width(),
            height = elem.height();
        var mopts = Highcharts.merge({title:"",subtitle:"",filename:"chart",highlightLast:false},opts);
        var chart = new Highcharts.Chart({
            chart: { renderTo: container, type: 'column', width: width, height: height, zoomType: 'x', panning: true, panKey: 'shift' },
            title: { text: (mopts.url ? '<a href="'+mopts.url+'">'+mopts.title+'</a>' : mopts.title), useHTML: mopts.url ? true : false },
            subtitle: { text: mopts.subtitle },
            xAxis: {
                categories: mopts.data.map(function(d) { return d.name}),
                labels: {
                    formatter: function() {
                        var ML = 8;
                        var vt = this.value.toString().trim();
                        var vl = vt.length;
                        var idx = Math.min(ML,vl);
                        var lbl = vt.slice(0,idx) + (idx < vl ? '…' : '');
                        return (mopts.highlightLast && this.isLast) ? '<strong>'+lbl+'</strong>' : lbl;
                    },
                    useHTML: false
                },
                minRange: 1
            },
            yAxis: { title: null, labels: { enabled: false } },
            legend: {shadow: false, enabled:mopts.categories.length>1},
            tooltip: {
                formatter: function () {
                    return '<b>' + this.x + '</b><br/>' +
                        this.series.name + ': ' + this.y + '<br/>' +
                        (mopts.ttitle ? '<br/>' +
                        mopts.ttitle+': ' + this.point.stackTotal : '');
                }
            },
            series: mopts.categories.map(function(d,idx) { return {name: d, data: mopts.data.map(function(e) { return e.data[idx]; })}; }),
            exporting: { scale: 3, filename: mopts.filename, enabled: true },
            plotOptions: { column: { stacking: 'normal' }, series: { animation: { duration: 0 } } }
        });
        return chart;
    },

    gaugeOptions: {
        chart: {
            type: 'solidgauge'
        },
        title: null,
        pane: {
            center: ['50%', '85%'],
            size: '100%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },
        tooltip: {
            enabled: false
        },
        // the value axis
        yAxis: {
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 200,
            tickWidth: 0,
            title: {
                y: -100,
                style: { color: '#a3a3a3', font: 'bold 18px sans-serif' }
            },
            labels: {
                y: 16
            }
        },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        }
    },
    createGauge: function createGauge(id,value,min,max,title,label,isCurrency) {
		$("#"+id).highcharts(Highcharts.merge(Charts.gaugeOptions, {
			yAxis: {
				min: min,
				max: max,
				title: {
					text: title
				}
			},
			series: [{
				name: title,
				data: [value],
				dataLabels: {
					format: '<div style="text-align:center"><span style="font-size:25px;color:' +
						((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">'+(isCurrency ? '$' : '')+'{y}</span><br/>' +
						   '<span style="font-size:12px;color:silver">'+label+'</span></div>'
				},
				tooltip: {
					valuePrefix: isCurrency ? '$' : '',
					valueSuffix: ' '+label
				}
        	}]}))}
};
