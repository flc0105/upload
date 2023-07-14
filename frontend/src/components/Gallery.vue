<template>
  <h2 class="pt-2 pb-2 border-bottom">{{ $t("gallery") }}</h2>
  <div class="gallery pt-3">
    <div v-for="(image, index) in images" :key="index" class="gallery-item">
      <img v-lazy="getLink(image)" class="w-100 shadow-1-strong rounded" />
      <!-- <img :src="getLink(image)" class="w-100 shadow-1-strong rounded" /> -->
      <div class="overlay">
        <div class="caption">
          {{ image.split("/").pop() }}
          <a :href="getLink(image)"
            ><i class="bi bi-cloud-download ms-2"></i
          ></a>
          <br />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  opacity: 0;
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
  -webkit-transition: all 0.4s ease-in-out;
  transition: all 0.4s ease-in-out;
}

.gallery-item:hover .overlay {
  opacity: 1;
  filter: alpha(opacity=100);
}

.overlay .caption a {
  color: white;
}

.overlay .caption {
  opacity: 0;
  text-transform: uppercase;
  color: #fff;
  text-align: center;
  position: absolute;
  right: 0;
  left: 0;
  bottom: 0;
  padding: 10px;
  /* background: rgba(0, 0, 0, 0.5); */
  margin: 0;
  -webkit-transition: all 0.5s ease-in-out;
  transition: all 0.5s ease-in-out;
  user-select: none;
}

.gallery-item:hover .overlay .caption {
  opacity: 1;
}

.gallery {
  column-count: 3; /* 将内容分为三列 */
  column-gap: 0.75rem; /* 设置列之间的间距 */
}

.gallery-item {
  width: 100%; /* 图片宽度为100% */
  break-inside: avoid; /* 防止图片被分割到不同的列中 */
  margin-bottom: 0.75rem; /* 设置图片之间的间距 */
  height: 100%;
  position: relative;
  cursor: default;
}

@media screen and (max-width: 768px) {
  .gallery {
    column-count: 1;
    column-gap: 0;
  }
}
</style>

<script>
import axios from "axios";
import "bootstrap/dist/js/bootstrap.bundle";
import VueLazyload from "vue-lazyload";

export default {
  data() {
    return {
      images: [],
    };
  },
  methods: {
    getLink(relativePath) {
      return (
        axios.defaults.baseURL +
        "file/download?relativePath=" +
        encodeURIComponent(relativePath)
      );
    },
    fetchImages() {
      const directory = this.$route.query.value; // 传递的目录参数
      axios
        .get(`/file/gallery?relativePath=${directory}`)
        .then((response) => {
          if (response.success) {
            this.images = response.detail;
            if (response.detail.length == 0) {
              this.$root.showModal(this.$t("alert"), this.$t("no_images"));
            }
          } else {
            console.error(response.msg);
          }
        })
        .catch((error) => {
          console.error(error);
        });
    },
  },
  mounted() {
    this.fetchImages();
    console.log(this.$route.query.value);
  },
};
</script>
