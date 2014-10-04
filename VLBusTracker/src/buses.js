/**
 * Created with JetBrains PhpStorm.
 * User: imshineckiy
 * Date: 3/26/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */

//TODO завeрнуть приложение в анонимную функцию, сделать минимум глобальных переменных.
//TODO Проверить потребление ресурсов

/**
 * Загрузка конфигруации
 */

var config = Config[0];
var BUSES_RELOAD_TIMEOUT = 5; // в секундах
var BUSES_MAX_TIME = 1800; // в секундах, максимальное время прибытия автобуса
var BUSES_MIN_TIME = 60; // в секундах, минимальное время прибытия автобуса

//namespace
var app = {};

/**
 * Хранилище для глобальных переменных, счетчиков и т.а.
 * @type {{}}
 */
app.storage = {};
app.storage.current_udid = null;

/**
 * конвертер array to object
 * @type {{stop: Function, route: Function}}
 */
app.convert = {
	stop: function (array) {
		return {
			id: array[0],
			name: array[1],
			coord: [array[3], array[2]],
			routes: array[4]
		};
	},
	route: function (array) {
		return {
			id: array[0],
			num: array[1],
			//geom: array[2],
			stops: array[2]
		};
	}
};

/**
 * Поиск остановки или маршрута по id
 * @type {{node: Function}}
 * @return array остановку или маршрут
 */
app.find = {
	node: function (id, array) {
		var result = false;
		$.each(array, function () {
			if (this[0] == id) {
				result = this;
			}
		});
		return result;
	}
};

$('#map').height($(window).innerHeight() - 210);  //70 - высота шапки
$(window).resize(function () {
	$('#map').height($(window).innerHeight() - 210);
});

// Карта
var map = L.map('map', {
	inertiaThreshold: 100,
	minZoom: 7
}).setView([config.center.lat, config.center.lon], 12);

//масштабная линейка
L.control.scale().addTo(map);

// Хэш с координатами
var hash = new L.Hash(map);

// Базовый слой
var baseLayer = L.tileLayer.wms(config.tilesServer, {
	layers: config.baseLayer,
	format: 'image/png',
	transparent: false,
	attribution: config.copyright
}).addTo(map);

// Дополнительные слои
var routeLayer = L.layerGroup().addTo(map);
var stopLayer = L.layerGroup();
var busLayer = L.layerGroup();

app.drawStopTimes = function (udid, data) {
	if (!data) data = app.storage.data;

	$.ajax({
		url: '/api/transport/bus/estimates',
		data: {
			udid: udid
		},
		success: function (response) {
			stopLayer.eachLayer(function (m) {
				$.each(response, function () {
					var id_stop = this[0];
					var time = Math.round(this[1]); //ответ от сервера в секундах

					if (m._popup.options.stop_id == id_stop) {
						if (time > BUSES_MAX_TIME) {
							time = BUSES_MAX_TIME;
							time = 'больше ' + Math.round(time / 60);
						} else if (time < BUSES_MIN_TIME) {
							time = BUSES_MIN_TIME;
							time = 'меньше ' + Math.round(time / 60);
						} else {
							time = Math.round(time / 60);
						}

						if (data && data.stops) {
							var stop = app.find.node(id_stop, data.stops);
							stop = app.convert.stop(stop);
							//$(m._icon).html('<div class="bus-time"> ост: ' + stop.name + ': ' + time + ' мин.</div>');
							$(m._icon).empty().html('<div title="ост: ' + stop.name + '" class="bus-time">' + time + ' мин.</div>');
						}
					}
				})
			});
		}
	});
};

busLayer.draw = function (devices, idRoute, groups, data) {
	var busesUdids = [];

	busLayer.eachLayer(function (bus) {
		busesUdids.push(bus.options.udid);
	});

	function getBusMarker(udid) {
		var ret = false;
		busLayer.eachLayer(function (marker) {
			if (marker.options.udid == udid) ret = marker;
		});
		return ret;
	}

	for (var i = 0; i < devices.length; i++) {
		var d = devices[i],
			g = groups ? (groups[d.id_group] ? groups[d.id_group] : '') : ''; // Наименование компании-перевозчика
		if (!idRoute || (d.id_route == idRoute)) {
			if (d.lat > 0 && d.lon > 0) {
				var marker;
				var icon;
				if (d.last_update < 300) {
					icon = busIcon(d);
				} else {
					icon = busIconInactive(d);
				}
				if ($.inArray(d.udid, busesUdids) == -1) {
					//рисую автобусы
					var popupInfo =
						'<div class="popup-route">' +
							'<div class="popup-stop-title">Маршрут №' + d.num + '</div>' +
							'<div class="popup-route-name">Номер автобуса: ' + d.name + '</div>';
					if (g && g.length) {
						popupInfo += '<div class="popup-route-name">Перевозчик: <a class="group-link" data-group_id="' + d.id_group + '">' + g + '</a></div>';
					}
					popupInfo += '</div>';


					marker = new L.Marker([d.lat, d.lon], {
						icon: icon,
						udid: d.udid
					});
					marker.device = d;
					this.addLayer(marker.bindPopup(popupInfo, {
						udid: d.udid,
						layout: 'bus',
						autoPan: false,
						opacity: 0.9,
						marker: marker
					}));
				} else {
					//передвигаю существующие автобусы
					marker = getBusMarker(d.udid);
					marker.setLatLng([d.lat, d.lon]).setIcon(icon).update();
				}


			}
		}
	}
	if (app.storage.current_udid) app.drawStopTimes(app.storage.current_udid, data);
};

busLayer.status = false;

//пробки
var trafficLayer = L.tileLayer.wms(config.trafficServer, {
	layers: config.trafficLayer.name,
	format: 'image/png',
	transparent: true,
	attribution: "Пробки"
});

app.redrawCustomLayer = function (url, layer) {
	$.ajax({
		url: url,
		dataType: 'json',
		headers: {
			'Cache-Control': 'max-age=86400'
		},
		success: function (result) {
			$.each(result.pois, function () {
				if (this.visibility) {
					var type = result.types[this.typeId];
					if (type && type.visibility) {
						var marker = L.marker([this.lat, this.lon], {
							icon: L.icon({
								iconUrl: type.thumb,
								iconSize: [type.width, type.height],
								popupAnchor: [0, -type.height * 0.7]
							})
						});
						layer.addLayer(marker.bindPopup(this.name));
					}
				}
			});
		}
	});
};

//Шиномонтаж
var tiresLayer = L.layerGroup();
tiresLayer.redraw = function () {
	return app.redrawCustomLayer('/json/poibygroup?id=5', tiresLayer);
};

// ДТП
var crushLayer = L.layerGroup();
crushLayer.redraw = function () {
	return app.redrawCustomLayer('/json/poibygroup?id=1', crushLayer);
};

//// AЗС (road-poi tile layer)
//var roadLayer = L.tileLayer.wms(config.tilesServer, {
//    layers: config.roadPoiLayer.name,
//    format: 'image/png',
//    transparent: true
//}).addTo(map);

// Контрол выбора слоёв
var baseLayers = {
	"MapVLru": baseLayer
};
var overlays = {
	"Автобусы": busLayer,
	"Остановки": stopLayer,
	"Маршруты": routeLayer,
	"Пробки": trafficLayer,
	//"АЗС"       : roadLayer,
	//"Шиномонтаж": tiresLayer,
	"ДТП": crushLayer
};
L.control.layers(baseLayers, overlays).addTo(map);

// Базовая иконка остановок (активная)
var stopIconActive = L.divIcon({
	iconSize: [38, 38],
	iconAnchor: [19, 19],
	popupAnchor: [0, -22],
	className: 'stop-active-div-icon'
});

// Базовая иконка остановок
var stopIcon = L.divIcon({
	iconSize: [24, 24],
	iconAnchor: [12, 12],
	popupAnchor: [0, -15],
	className: 'stop-div-icon'
});

//Дефолтная иконка для всех указателей
var defaultIcon = L.divIcon({
	iconSize: [25, 41],
	iconAnchor: [12, 41],
	popupAnchor: [0, -22],
	className: 'default-div-icon'
});

//Базовая иконка автобуса
var busIcon = function (d) {
	return L.divIcon({
		iconSize: [55, 55],
		iconAnchor: [27, 27],
		popupAnchor: [0, -19],
		className: 'bus-div-icon',
		html: d.num,
		bgPos: {
			x: 0,
			y: -Math.floor((360 - d.bearing) / 360 * 24) * 55
		}
	});
};
var busIconInactive = function (d) {
	return L.divIcon({
		iconSize: [55, 55],
		iconAnchor: [27, 27],
		popupAnchor: [0, -19],
		className: 'bus-div-icon bus-div-icon-inactive',
		html: d.num,
		bgPos: {
			x: 0,
			y: -Math.floor((360 - d.bearing) / 360 * 24) * 55
		}
	});
};

// Периодическое автообновление
window.timeoutsStore = {};
window.timeoutsStore.buses = [];

var redrawWorker = function (data) {
	// Устройства
	$.ajax({
		url: '/api/transport/bus/current',
		data: {
			id_route: map.routeId || false,
			age: config.activeBusesAge || null
		},
		dataType: 'json',
		success: function (devices) {
			map.buses = [];
			for (var i = 0; i < devices.length; i++) {
				var d = devices[i];
				map.buses.push({
					'udid': d[0],
					'name': d[1],
					'id_route': d[2],
					'lon': d[3],
					'lat': d[4],
					'bearing': d[5],
					'num': d[6],
					'id_group': d[7],
					'last_update': d[8]
				});
			}
			busLayer.draw(map.buses, map.routeId, app.groups, data);
		}
	});

	//удаляю все timeouts из хранилища и очищаю его
	$.each(window.timeoutsStore.buses, function (i, timeout) {
		clearTimeout(timeout);
	});
	window.timeoutsStore.buses = [];

	var timeout = setTimeout(redrawWorker, BUSES_RELOAD_TIMEOUT * 1000);
	window.timeoutsStore.buses.push(timeout);
};


app.fav = {
	fav_stops: [],
	fav_routes: []
};

app.getPopupHTML = function (stop) {
	var template;
	template =
		'<div class="popup-stop">' +
			'<div class="popup-stop-title">ост. ' + stop.name + '</div>' +
			'<div class="popup-stop-loader"><div></div></div>' +
			'<div class="popup-stop-routes-wrapper"><table class="popup-stop-routes"><tr><th>Маршрут</th><th>Время прибытия</th></tr></table></div>' +
//			'<div class="popup-actions"><span data-id_stop="' + stop.id + '" class="sroutes-start">туда</span> <span data-id_stop="' + stop.id + '" class="sroutes-end">сюда</span></div>' +
		'</div>';
	return template;
};

// Рисую остановки
app.drawStops = function (data, id_route) {
	map.markersList = {};
	stopLayer.clearLayers();

	app.drawStopMarkerInvisible = function (stop, hide) {
		var hide = hide || false;
		var popup = app.getPopupHTML(stop);
		var marker = L.marker(stop.coord, {
			icon: stopIcon
		});
		if (hide) {
			marker.setOpacity(0.7);
		}
		stopLayer.addLayer(marker.bindPopup(popup, {
			stop_id: stop.id,
			layout: 'stop',
			currentmarker: marker,
			hidemarker: 1
		})); //передаю в options id текущей остановки, для использования в event 'popupopen'
		map.markersList['stop-' + stop.id] = marker; //Массив маркеров в памяти для доуступа из триггеров
		marker.on('click', function () {
			this.setOpacity(1);
		});
	};

	app.drawStopMarkerVisible = function (stop) {
		var popup = app.getPopupHTML(stop);
		var marker = L.marker(stop.coord, {
			icon: stopIconActive,
			opacity: 1
		});
		stopLayer.addLayer(marker.bindPopup(popup, {
			stop_id: stop.id,
			layout: 'stop'
		})); //передаю в options id текущей остановки, для использования в event 'popupopen'
		map.markersList['stop-' + stop.id] = marker; //Массив маркеров в памяти для доуступа из триггеров
	};

	var id_route = id_route || false;

	if (!id_route) {
		$.each(data.stops, function () {
			app.drawStopMarkerInvisible(app.convert.stop(this));
		});
	} else {
		$.each(data.stops, function () {
			var stop = app.convert.stop(this);
			app.drawStopMarkerInvisible(stop, true);
		});
		var stopsActive = [];
		var route = app.find.node(id_route, data.routes);
		route = app.convert.route(route);
		stopsActive.push(this.id_stop);

		stopLayer.eachLayer(function (marker) {
			if ($.inArray(marker._popup.options.stop_id, route.stops) != -1) {
				marker.setIcon(stopIconActive).update();
				marker.setOpacity(1);
			}
		});
	}
};
// Попапы на остановках
app.stopsPopupBind = function (data) {
	map.on('popupopen', function (e) {
		if (e.popup.options.layout && e.popup.options.layout == 'stop') {

			if (e.popup.options.currentmarker) {
				var elem = L.DomUtil.get(e.popup.options.currentmarker._icon);
				$(elem).addClass('stop-active-div-icon').removeClass('stop-div-icon');
			}

			var id_stop = e.popup.options.stop_id;
			var popup_content = $('.popup-stop-routes', e.popup._contentNode);
			var id_routes = [];
			var stop = app.find.node(id_stop, data.stops);
			stop = app.convert.stop(stop);
			var template = '';
			$.each(stop.routes, function (i) {
				var item_class = '';
				if (i > 2) item_class = 'hidden';
				var route = app.find.node(this, data.routes);
				route = app.convert.route(route);
				template += '<tr class="' + item_class + '"><td><div class="popup-stop-routes-item"><strong data-id_route="' + route.id + '" >' + route.num + '</strong></td><td class="time"><i data-id_route="' + route.id + '"></i> <span>мин.</span></td></tr>'
				//var item = '<i class="time">Прибытие: <span>' + route.name + '</span></div>';
				if (i == 2) {
					template += '<tr><td colspan="2"><span class="more">Все маршруты</span></td></tr>';
				}
				id_routes.push(route.id);
			});
			popup_content.append(template);

			$('.popup-stop-routes .more').unbind('click').bind('click', function () {
				$('.popup-stop-routes .hidden').removeClass('hidden');
				$(this).parents('tr').remove();
			});

			// Время прибытия автобусов на остановку
			function updateRoutesTimes() {

				function stoploader() {
					$('.popup-stop-loader div').width(0).stop(true, true).animate({
						width: 260
					}, BUSES_RELOAD_TIMEOUT * 1000, function () {
						stoploader();
					});
				}

				stoploader();

				function RoutesTimesRender(response) {
					$('.popup-stop-routes .time i').each(function () {
						var id_route = $(this).data('id_route'),
							seconds = 0,
							bus_udid = '';
						for (var z = 0; z < response.length; z++) {
							if (response[z][0] === id_route) {
								bus_udid = response[z][1];
								seconds = response[z][2];
								break;
							}
						}
						var container = $(this).parents('.time');

						if (seconds) {
							if (seconds < 60) {
								container.find('i').text('').siblings('span').text('меньше 60 секунд');
							} else {
								container.find('i').text(Math.round(seconds / 60)).siblings('span').text('мин.');
							}
							container.show();
						} else {
							container.find('i').empty().html('-').siblings('span').empty();
						}
					});
				}

				$.ajax({
					url: '/api/transport/stop/estimates',
					data: {
						id_node: stop.id
					},
					success: function (response) {
						// Возвращается массив массивов вида [id_route, udid, interval]
						RoutesTimesRender(response);
					}
				});
			}

			updateRoutesTimes();

			//удаляю все timeouts из хранилища и очищаю его
			if (window.timeoutsStore.routestimes && window.timeoutsStore.routestimes.length) {
				$.each(window.timeoutsStore.routestimes, function (i, timeout) {
					clearInterval(timeout);
				});
			}

			window.timeoutsStore.routestimes = [];
			var timeout = setInterval(updateRoutesTimes, BUSES_RELOAD_TIMEOUT * 1000);
			window.timeoutsStore.routestimes.push(timeout);

			//Trigger выбора маршрута в попапе
			$('.popup-stop-routes-item strong').unbind('click').bind('click', function () {
				app.stopsPopupTrigger($(this), data);
			});

		}
	});

	map.on('popupclose', function (e) {
		//отмена аякса для обновления времени маршрутов в попапе остановки
		clearInterval(app.storage.updateRoutesTimes);

		//удаляю все timeouts из хранилища и очищаю его
		if (window.timeoutsStore.routestimes && window.timeoutsStore.routestimes.length) {
			$.each(window.timeoutsStore.routestimes, function (i, timeout) {
				clearInterval(timeout);
			});
		}

		if (e.popup.options.layout && e.popup.options.layout == 'stop') {
			//убираю иконку у остановки - если она не в маршруте или щелкнул в стороне.
			if (e.popup.options.hidemarker) {
				var elem = L.DomUtil.get(e.popup.options.currentmarker._icon);
				$(elem).removeClass('stop-active-div-icon').addClass('stop-div-icon');
			}
		}
	});
};

app.stopsPopupTrigger = function ($el, data) {
	var id_route = $el.data('id_route');
	app.drawStops(data, id_route);
	app.getSegmentById(id_route);
	map.routeId = id_route;

	//Удаляю текущие автобусы, и рисую новые
	busLayer.clearLayers();
	redrawWorker(data);
};

// Попапы на автобусах
app.busPopupBind = function (data) {
	map.on('popupopen', function (e) {
		$('.bus-time').remove();
		if (e.popup.options.layout && e.popup.options.layout == 'bus') {
			busLayer.eachLayer(function (m) {
				m.setOpacity(0);
			});
			e.popup.options.marker.setOpacity(1);

			app.drawStopTimes(e.popup.options.udid, data);
			app.storage.current_udid = e.popup.options.udid;
		}

		$('.group-link').off('click').on('click', function (e) {
			$.ajax({
				url: '/api/transport/bus/group',
				data: {
					id: $(this).data('group_id')
				},
				success: function (response) {
					var container = $('#group-info-container');
					container.empty();
					if (response.data.description == null) response.data.description = '';

					if (response.data) {
						container.append('<div class="name">' + response.data.name + '</div>');
						container.append('<div class="phone">Тел: ' + response.data.phone + '</div>');
						container.append('<div class="description">' + response.data.description + '</div>');
						$('.tab-controls a, .tab').removeClass('active');
						$('#tab-groupinfo').addClass('active');
					}
				}
			});
		});
	});

	map.on('popupclose', function (e) {
		$('.bus-time').remove();
		app.storage.current_udid = null;

		busLayer.eachLayer(function (m) {
			m.setOpacity(1);
		});
	});
};

app.BusesControl = L.Control.extend({
	options: {
		position: 'topright'
	},
	onAdd: function (map) {
		var container = L.DomUtil.create('div', 'leaflet-control-buses');
		var link = L.DomUtil.create('a', 'leaflet-control-buses-btn ' + busLayer.status, container);
		$(link).text('Автобусы');
		L.DomUtil.create('span', 'state', link);
		return container;
	}
});

//Левая колонка
app.sidebar = function () {

	var sidebar = $('#sidebar');
	var sidebar_btns = $('#sidebar-button').add('.leaflet-control-buses-btn');
	var leaflet_left = $('.leaflet-left');

	$(document).on('click', '.leaflet-control-buses-btn', function () {
		if (busLayer.status) {
			map.removeLayer(busLayer);
			map.removeLayer(stopLayer);
			map.removeLayer(routeLayer);
			busLayer.status = false;
		} else {
			map.addLayer(busLayer);
			map.addLayer(stopLayer);
			map.addLayer(routeLayer);
			busLayer.status = true;
		}
		;
		$(this).removeClass('true false').addClass(busLayer.status.toString());
		$(document).trigger('click_busbtn', {status: busLayer.status});
	});

	//Tabs
	$(document).on('click', '.tab-controls a', function () {
		var tab_id = $(this).attr('href');
		$('.tab-controls a, .tab').removeClass('active');
		$(this).add(tab_id).addClass('active');
		return false;
	});

	map.on('layeradd layerremove', function (e) {
		busLayer.status = map.hasLayer(busLayer);
		$('.leaflet-control-buses-btn').removeClass('true false').addClass(busLayer.status.toString());
	});

	sidebar_btns.on('click', function () {
		if (busLayer.status) {
			sidebar.stop().animate({
				left: -250
			}, function () {
				busLayer.status = false;
				sidebar.trigger('closed').removeClass('true false').addClass(busLayer.status.toString());
				;
			});
			leaflet_left.stop().animate({
				left: 0
			});
		} else {
			sidebar.stop().animate({
				left: 0
			}, function () {
				busLayer.status = true;
				sidebar.trigger('opened').removeClass('true false').addClass(busLayer.status.toString());
				;
			});
			leaflet_left.stop().animate({
				left: 250
			});
		}
	});
};

app.appendSearchEvents = function (data) {
	//Event выбора остановки в попапе
	$('.widget-search-routes strong.item-stop').add('#favorites-container .stop span').unbind('click').bind('click', function () {
		var marker = map.markersList['stop-' + $(this).data('id_stop')];
		map.setView(marker._latlng, map._zoom);
		setTimeout(function () {  //Задержка - жду пока карта закончит анимацию.
			marker.openPopup();
			$(L.DomUtil.get(marker._icon)).css('opacity', 1);
		}, 600);
	});

	//Event выбора маршрута в попапе
	$('.widget-search-routes strong.item-route').add('#favorites-container .route span').unbind('click').bind('click', function () {
		app.stopsPopupTrigger($(this), data);
	});
};

app.widgetBusesSearch = function (data) {
	var sidebar = $('#tab-buses');

	sidebar.append('<div class="widget widget-search widget-search-routes">');
	$('.widget-search-routes').append('<div class="form"><input type="text" name="search-routes" placeholder="например: Луговая" id="input-search-routes"/><div></div>');
	$('.widget-search-routes').append('<div id="search-routes-result">');
	var search_result = $('#search-routes-result');

	var h = 115;
	search_result.height($('#map').height() - h);
	$('#tab-globalsearch').height($('#map').height() - h);
	$('#map').resize(function () {
		search_result.height($('#map').height() - h);
		$('#tab-globalsearch').height($('#map').height() - h);
	});

	function search_result_empty() {
		search_result.empty();
	};

	function appendResultStopTitle() {
		search_result.append('<h3>Остановки</h3>');
	};

	function appendItemStopHtml(stop) {
		var classes = '';
		if ($.inArray(stop.id.toString(), app.fav.fav_stops) != -1) {
			classes += 'active';
		}
		var item = '<div class="item"><i title="Добавить в избранное" class="icon-favorite ' + classes + '" data-id_stop="' + stop.id + '"></i><strong class="item-stop" data-id_stop="' + stop.id + '" >' + stop.name + '</strong></div>';
		search_result.append(item);
	};

	function appendResultRouteTitle() {
		search_result.append('<h3>Маршруты</h3>');
	};

	function appendItemRouteHtml(route) {
		var classes = '';
		if ($.inArray(route.id.toString(), app.fav.fav_routes) != -1) {
			classes += 'active';
		}
		//var item = '<div class="item"><i title="Добавить в избранное" class="icon-favorite ' + classes + '" data-id_route="' + route.id + '"></i><strong class="item-route" data-id_route="' + route.id + '" >' + route.num + '</strong><span>' + route.name + '</span></div>';
		var item = '<div class="item"><i title="Добавить в избранное" class="icon-favorite ' + classes + '" data-id_route="' + route.id + '"></i><strong class="item-route" data-id_route="' + route.id + '" >' + route.num + '</strong><br></div>';
		search_result.append(item);
	};

	search_result_empty();
	search_result.append('<div class="empty">Ничего не найдено.</div>');

	$('#input-search-routes').on('keyup', function () {
		var val = $(this).val().trim();
		search_result_empty();
		appendResultRouteTitle();
		var routes_count = 0;
		$.each(data.routes, function () {
			var route = app.convert.route(this);
			if ((new RegExp(val, 'gim').test(route.num)) && val) {
				//if ((this.num.indexOf(val) != -1 || this.name.indexOf(val) != -1) && val) {
				appendItemRouteHtml(route);
				routes_count++;
			}
		});
		if (!routes_count) {
			search_result.append('<div class="empty">Ничего не найдено.</div>');
		}

		appendResultStopTitle();
		var stop_count = 0;
		$.each(data.stops, function () {
			var stop = app.convert.stop(this);
			if ((new RegExp(val, 'gim').test(stop.name)) && val) {
				//if (this.name.indexOf(val) != -1 && val) {
				appendItemStopHtml(stop);
				stop_count++;
			}
		});
		if (!stop_count) {
			search_result.append('<div class="empty">Ничего не найдено.</div>');
		}
		app.appendSearchEvents(data);
	});

	app.appendSearchEvents(data);
};

//рисовалка маршрута
app.getSegmentById = function (id_route) {
	$.ajax({
		url: '/api/transport/bus/routepath',
		data: {
			id: id_route
		},
		dataType: 'json',
		headers: {
			'Cache-Control': 'max-age=86400'
		},
		success: function (routePath) {
			var geom = JSON.parse(routePath.geojson);
			// Группа для фокусировки на маршруте
			var routeFeature = L.geoJson(geom, {
				"type": "Feature",
				"properties": {
					"id": routePath.id
				},
				"geometry": geom,
				"style": function (feature) {
					return { color: "#15aaf8", weight: 6, opacity: 0.8 };
				}
			});
			routeLayer.clearLayers().addLayer(routeFeature);
			map.fitBounds(routeFeature.getBounds());
		}
	});
};

app.isLocalStorageAvailable = function () {
	try {
		return 'localStorage' in window && window['localStorage'] !== null;
	} catch (e) {
		return false;
	}
};

app.favoriteSave = function (nameStorage, id) {
	var items = localStorage.getItem(nameStorage);
	if (items) {
		items = items.split(',');
	} else {
		items = [];
	}
	var saved = $.inArray(id.toString(), items);
	if (saved == -1) {
		items.push(id.toString());
	} else {
		delete items[saved];
		var result = [];
		for (var i = 0; i < items.length; i++) {
			if (i in items) {
				result.push(items[i]);
			}
		}
		items = result;
	}
	app.fav[nameStorage] = items;
	localStorage.setItem(nameStorage, items.slice(','));
};

app.favoriteGetItems = function (nameStorage) {
	var items = localStorage.getItem(nameStorage);
	if (items) {
		items = items.split(',');
	} else {
		items = [];
	}
	app.fav[nameStorage] = items;
};

app.favoriteTooltip = function (data) {
	var container = $('#favorites-container');
	container.empty();
	$.each(app.fav['fav_routes'], function (k, id) {
		var route = app.find.node(id, data.routes);
		route = app.convert.route(route);
		container.append('<div class="item route"><i title="Удалить" class="icon-favorite active" data-id_route="' + route.id + '">x</i><span data-id_route="' + route.id + '">Маршрут №' + route.num + '</span></div>');
	});
	$.each(app.fav['fav_stops'], function (k, id) {
		var stop = app.find.node(id, data.stops);
		stop = app.convert.stop(stop);
		if (stop) {
			container.append('<div class="item stop"><i title="Удалить" class="icon-favorite active" data-id_stop="' + stop.id + '">x</i><span data-id_stop="' + stop.id + '">Ост: ' + stop.name + '</span></div>');
		}
	});
	if (!app.fav['fav_stops'].length && !app.fav['fav_routes'].length) {
		container.append('<div class="empty">Пусто</div>')
	}
	app.appendSearchEvents(data);
};

app.favorite = function (data) {
	//init
	app.favoriteGetItems('fav_stops');
	app.favoriteGetItems('fav_routes');
	app.favoriteTooltip(data);

	$(document).on('click', '.icon-favorite', function (e) {
		e.preventDefault();
		var $data = $(this).data();
		if ($data && $data.id_stop) {
			app.favoriteSave('fav_stops', $data.id_stop);
			$(this).toggleClass('active');
		}
		if ($data && $data.id_route) {
			app.favoriteSave('fav_routes', $data.id_route);
			$(this).toggleClass('active');
		}
		app.favoriteTooltip(data);
		$('#input-search-routes').trigger('keyup');
	});
};

app.globalSearch = function () {
	var search = new L.Search(map, defaultIcon);

	$(document).on('globalsearch', function (e, data) {
		search.searchByAddress(data.value, '#tab-globalsearch');
		$('.tt-dropdown-menu').hide();
	});

	$('.search form').submit(function () {
		var value = $('#global-search').val();
		$(document).trigger('globalsearch', {
			value: value
		});
		return false;
	});

	$('#global-search').typeahead({
		name: 'global_search',
		limit: 20,
		remote: {
			url: '/search/autocomplete/?query=%QUERY',
			filter: function (query) {
				return query.suggestions;
			}
		}
	}).bind('typeahead:selected', function (e) {
			$(document).trigger('globalsearch', {
				value: this.value
			});
		});

};

app.busesPage = function (data) {
	//Включаю слой автобуса./traffic/buses
	if (window.location.pathname.indexOf('/traffic/buses') > -1) {
		$('.leaflet-control-buses-btn').not('true').trigger('click');
	}

	redrawWorker(data);
};

app.tailEvents = function(){
	map.on('click', function (e) {
		$.ajax({
			url: '/identifylonlat/',
			crossDomain: true,
			dataType: 'jsonp',
			data: {
				lat: e.latlng.lat,
				lon: e.latlng.lng,
				zoom: e.target._zoom
			},
			success: function(response){
				if (response.builds && response.builds.length > 0) {
					var popText = '';
					$.each(response.builds, function(){
						popText += '<div class="item">';
						popText += '<div class="address">' + this.address + '</div>';
						if (this.caption.length) popText += '<div class="caption">' + this.caption + '</div>';
						popText += '</div>';
					});

					L.popup().setLatLng(e.latlng).setContent(popText).openOn(map);
				}
			}
		});
	});
};

$.ajax({
	url: '/api/transport/bus/aggregate',
	dataType: 'json',
	headers: {
		'Cache-Control': 'max-age=86400'
	},
	success: function (response) {
		/**
		 * если ответ от сервера вернул пустой результат или ошибку
		 *
		 * */
		if (!response || !response.success) {
			return false;
		}

		/**
		 * TODO сделать сохранение данных в локальный кэш
		 */
		var data = response.data;
		app.storage.data = data;

		// Группы устройств -- они же компании-перевозчики
		app.groups = data.groups;

		// Рисую остановки
		app.drawStops(data);

		// Рисую popups
		app.stopsPopupBind(data);

		app.busPopupBind(data);

		// Кнопка "Автобусы"
		map.addControl(new app.BusesControl());

		// Рисую левую колонку
		app.sidebar();

		// Рисую виджет поиска в левой колонке
		app.widgetBusesSearch(data);

		// Избранное пользователя
		app.favorite(data);

		// Индивидуальный функционал
		app.busesPage(data);

		// Слой автобусов
		$(document).on('click_busbtn', function (e, params) {
			if (params.status) {
				redrawWorker(data);
			} else {
				//удаляю все timeouts из хранилища и очищаю его
				$.each(window.timeoutsStore.buses, function (i, timeout) {
					clearTimeout(timeout);
				});
				window.timeoutsStore.buses = [];
			}
		});

		//Событие клика по карте и вывод балуна с информацией о точке
		app.tailEvents();
	}
});

$(document).ready(function () {
	app.globalSearch();
//    tiresLayer.redraw();
	crushLayer.redraw();

	$('.modal .close, .modal-back').click(function (e) {
		e.preventDefault();
		$('.modal-wrap').addClass('hide');
	});
});
