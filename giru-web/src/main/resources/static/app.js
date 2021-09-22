var dt_header_template =
	`<i-header>
        <div class="header-logo">Giru<span style="color:red;"></span></div>
        <div class="header-user"><icon type="ios-contact" /> {{userName}}</div>
        <i-menu mode="horizontal" theme="light" active-name="1" @on-select="go">
          <menu-item v-for="menu in dtConfig.menus" :name="menu.title" :key="menu.title">
            <icon :type="menu.icon" :color="menu.color" size="20"></icon><span style="vertical-align: middle;line-height: 20px;">{{menu.title}}</span>
          </menu-item>
        </i-menu>
      </i-header>`;

Vue.component('dt-header', {
	template: dt_header_template,
	methods: {
		go: function (name) {
			window.location.href = this.menuMap[name].url;
		}
	},
	mounted: function () {
		var self = this;
		axios.get('/admin/user').then(function (response) {
			self.userName = response.data.map.user;
		}).catch(function (error) {
			console.error(error.response);
			location.reload();
		});
	},
	data: function () {
		var menuMap = {};
		dtConfig.menus.map(function (menu) {
			menuMap[menu.title] = menu;
		});
		return {
			dtConfig: dtConfig,
			menuMap: menuMap,
			userName: ""
		}
	}
})

//日期时间
Vue.component('date-time', {
	props: ['time', 'inline'],
	template: '<span v-if="!inline"><nobr>{{d}}</nobr></br><nobr>{{t}}</nobr></span><span v-else><nobr>{{d}} {{t}}</nobr></span>',
	computed: {
		d: function () {
			if (!this.time) {
				return "--";
			}
			return moment(this.time).format("YYYY-MM-DD")
		},
		t: function () {
			if (!this.time) {
				return "";
			}
			return moment(this.time).format("HH:mm:ss");
		}
	}
});

var DtUtil = {
	requestNotify: function () {
		Notification.requestPermission(function (status) {
			console.log(status);
			var n = new Notification("title", {body: "notification body"}); // 显示通知
		});
	}
}


function toQueryString(obj) {
	return new URLSearchParams(obj).toString();
	// Object.keys(obj).map(function(key) {
	// 		return encodeURIComponent(key) + '=' +
	// 			encodeURIComponent(obj[key]);
	// 	}).join('&');
}
