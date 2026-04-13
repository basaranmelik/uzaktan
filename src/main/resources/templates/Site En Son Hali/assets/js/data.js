// ============================================
// GUZEM — Dummy Data (Turkish)
// ============================================

const COURSES = [
  {
    id: 1,
    title: "Python ile Veri Bilimi",
    category: "Veri Bilimi",
    description: "Python programlama dili ile veri analizi, görselleştirme ve makine öğrenmesi temelleri.",
    instructor: "Dr. Ahmet Yılmaz",
    instructorId: 1,
    duration: "42 Saat",
    lessons: 68,
    students: 2450,
    rating: 4.8,
    ratingCount: 312,
    price: "₺299",
    oldPrice: "₺599",
    level: "Başlangıç",
    image: "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "Python Temelleri", lessons: ["Değişkenler ve Veri Tipleri", "Kontrol Yapıları", "Fonksiyonlar", "Modüller"], durations: ["15 dk", "20 dk", "25 dk", "18 dk"] },
      { section: "Veri Analizi", lessons: ["NumPy Giriş", "Pandas ile Veri İşleme", "Veri Temizleme", "İstatistiksel Analiz"], durations: ["22 dk", "30 dk", "28 dk", "25 dk"] },
      { section: "Görselleştirme", lessons: ["Matplotlib Temelleri", "Seaborn ile Grafikler", "Plotly İnteraktif"], durations: ["20 dk", "25 dk", "22 dk"] }
    ]
  },
  {
    id: 2,
    title: "Modern Web Geliştirme",
    category: "Web Geliştirme",
    description: "HTML, CSS, JavaScript ve React ile modern ve responsive web uygulamaları geliştirme.",
    instructor: "Elif Kaya",
    instructorId: 2,
    duration: "56 Saat",
    lessons: 94,
    students: 3120,
    rating: 4.9,
    ratingCount: 428,
    price: "₺349",
    oldPrice: "₺699",
    level: "Orta",
    image: "https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "HTML & CSS", lessons: ["HTML5 Temelleri", "CSS Grid & Flexbox", "Responsive Tasarım", "Animasyonlar"], durations: ["18 dk", "25 dk", "22 dk", "20 dk"] },
      { section: "JavaScript", lessons: ["ES6+ Özellikler", "DOM Manipülasyonu", "Async/Await", "API Entegrasyonu"], durations: ["28 dk", "30 dk", "25 dk", "22 dk"] },
      { section: "React", lessons: ["Component Yapısı", "State & Props", "Hooks", "Router"], durations: ["25 dk", "28 dk", "30 dk", "22 dk"] }
    ]
  },
  {
    id: 3,
    title: "Dijital Pazarlama Uzmanlığı",
    category: "Pazarlama",
    description: "SEO, sosyal medya, Google Ads ve içerik pazarlaması stratejileriyle dijital dünyada fark yaratın.",
    instructor: "Mehmet Demir",
    instructorId: 3,
    duration: "38 Saat",
    lessons: 52,
    students: 1890,
    rating: 4.7,
    ratingCount: 245,
    price: "₺249",
    oldPrice: "₺499",
    level: "Başlangıç",
    image: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "SEO Temelleri", lessons: ["Anahtar Kelime Araştırma", "Teknik SEO", "İçerik Optimizasyonu"], durations: ["25 dk", "30 dk", "20 dk"] },
      { section: "Sosyal Medya", lessons: ["Platform Stratejileri", "İçerik Takvimi", "Reklam Yönetimi"], durations: ["22 dk", "18 dk", "28 dk"] }
    ]
  },
  {
    id: 4,
    title: "UI/UX Tasarım Bootcamp",
    category: "Tasarım",
    description: "Kullanıcı deneyimi ve arayüz tasarımının temellerinden ileri düzeye kapsamlı eğitim.",
    instructor: "Zeynep Arslan",
    instructorId: 4,
    duration: "45 Saat",
    lessons: 72,
    students: 1560,
    rating: 4.9,
    ratingCount: 198,
    price: "₺399",
    oldPrice: "₺799",
    level: "Orta",
    image: "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "UX Temelleri", lessons: ["UX Araştırma", "Persona Oluşturma", "User Flow", "Wireframing"], durations: ["20 dk", "25 dk", "22 dk", "28 dk"] },
      { section: "UI Tasarım", lessons: ["Renk Teorisi", "Tipografi", "Figma Masterclass", "Design System"], durations: ["18 dk", "22 dk", "35 dk", "30 dk"] }
    ]
  },
  {
    id: 5,
    title: "Yapay Zeka ve Derin Öğrenme",
    category: "Yapay Zeka",
    description: "Sinir ağları, CNN, RNN ve transformer modelleriyle yapay zeka projelerinizi hayata geçirin.",
    instructor: "Dr. Ahmet Yılmaz",
    instructorId: 1,
    duration: "62 Saat",
    lessons: 86,
    students: 2100,
    rating: 4.8,
    ratingCount: 276,
    price: "₺449",
    oldPrice: "₺899",
    level: "İleri",
    image: "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "Makine Öğrenmesi", lessons: ["Regresyon", "Sınıflandırma", "Kümeleme", "Model Değerlendirme"], durations: ["30 dk", "28 dk", "25 dk", "22 dk"] },
      { section: "Derin Öğrenme", lessons: ["Sinir Ağları Temeli", "CNN", "RNN/LSTM", "Transformer"], durations: ["35 dk", "40 dk", "38 dk", "42 dk"] }
    ]
  },
  {
    id: 6,
    title: "Mobil Uygulama Geliştirme",
    category: "Mobil",
    description: "React Native ile iOS ve Android platformlarında profesyonel mobil uygulamalar geliştirin.",
    instructor: "Can Özkan",
    instructorId: 5,
    duration: "48 Saat",
    lessons: 78,
    students: 1340,
    rating: 4.6,
    ratingCount: 167,
    price: "₺379",
    oldPrice: "₺759",
    level: "Orta",
    image: "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=600&h=400&fit=crop",
    featured: true,
    curriculum: [
      { section: "React Native Temelleri", lessons: ["Kurulum", "Component'ler", "Navigation", "State Management"], durations: ["15 dk", "28 dk", "25 dk", "30 dk"] },
      { section: "İleri Konular", lessons: ["API Entegrasyonu", "Push Notifications", "App Store Yayınlama"], durations: ["25 dk", "22 dk", "20 dk"] }
    ]
  },
  {
    id: 7,
    title: "Siber Güvenlik Temelleri",
    category: "Güvenlik",
    description: "Ağ güvenliği, etik hacking ve güvenlik açıkları analizi konularında temel eğitim.",
    instructor: "Burak Şahin",
    instructorId: 6,
    duration: "35 Saat",
    lessons: 48,
    students: 980,
    rating: 4.7,
    ratingCount: 134,
    price: "₺329",
    oldPrice: "₺659",
    level: "Başlangıç",
    image: "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600&h=400&fit=crop",
    featured: false,
    curriculum: [
      { section: "Güvenlik Temelleri", lessons: ["Ağ Güvenliği", "Kriptografi", "Güvenlik Duvarları"], durations: ["25 dk", "30 dk", "22 dk"] },
      { section: "Etik Hacking", lessons: ["Bilgi Toplama", "Zafiyet Tarama", "Penetrasyon Testi"], durations: ["28 dk", "35 dk", "30 dk"] }
    ]
  },
  {
    id: 8,
    title: "Flutter ile Cross-Platform",
    category: "Mobil",
    description: "Google'ın Flutter framework'ü ile tek kod tabanından mobil, web ve desktop uygulamalar.",
    instructor: "Can Özkan",
    instructorId: 5,
    duration: "44 Saat",
    lessons: 66,
    students: 890,
    rating: 4.5,
    ratingCount: 112,
    price: "₺349",
    oldPrice: "₺699",
    level: "Orta",
    image: "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=600&h=400&fit=crop",
    featured: false,
    curriculum: [
      { section: "Dart & Flutter Giriş", lessons: ["Dart Programlama", "Widget Yapısı", "Layout Yönetimi"], durations: ["25 dk", "30 dk", "28 dk"] },
      { section: "İleri Flutter", lessons: ["State Management", "Firebase Entegrasyonu", "Animasyonlar"], durations: ["35 dk", "30 dk", "25 dk"] }
    ]
  },
  {
    id: 9,
    title: "DevOps ve CI/CD Pipeline",
    category: "DevOps",
    description: "Docker, Kubernetes, Jenkins ve GitLab CI ile modern DevOps süreçlerini öğrenin.",
    instructor: "Burak Şahin",
    instructorId: 6,
    duration: "40 Saat",
    lessons: 58,
    students: 760,
    rating: 4.8,
    ratingCount: 98,
    price: "₺399",
    oldPrice: "₺799",
    level: "İleri",
    image: "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=600&h=400&fit=crop",
    featured: false,
    curriculum: [
      { section: "Docker", lessons: ["Container Temelleri", "Dockerfile Yazma", "Docker Compose"], durations: ["22 dk", "28 dk", "25 dk"] },
      { section: "Kubernetes", lessons: ["Pod & Service", "Deployment", "Helm Charts"], durations: ["30 dk", "35 dk", "28 dk"] }
    ]
  }
];

const INSTRUCTORS = [
  {
    id: 1,
    name: "Dr. Ahmet Yılmaz",
    title: "Veri Bilimi & Yapay Zeka Uzmanı",
    bio: "15 yıllık akademik ve endüstri deneyimiyle veri bilimi alanında öncü. Stanford'da misafir araştırmacı olarak çalıştı.",
    avatar: "AY",
    courses: 5,
    students: 4550,
    rating: 4.8,
    expertise: ["Python", "Machine Learning", "Deep Learning", "NLP"],
    social: { linkedin: "#", twitter: "#", github: "#" }
  },
  {
    id: 2,
    name: "Elif Kaya",
    title: "Senior Full-Stack Developer",
    bio: "10 yılı aşkın web geliştirme deneyimi. Google ve Meta'da çalışmış, açık kaynak projelerde aktif katkıcı.",
    avatar: "EK",
    courses: 4,
    students: 3120,
    rating: 4.9,
    expertise: ["React", "Node.js", "TypeScript", "Next.js"],
    social: { linkedin: "#", twitter: "#", github: "#" }
  },
  {
    id: 3,
    name: "Mehmet Demir",
    title: "Dijital Pazarlama Stratejisti",
    bio: "Uluslararası markaların dijital dönüşüm projelerinde danışmanlık yapan, Google sertifikalı pazarlama uzmanı.",
    avatar: "MD",
    courses: 3,
    students: 1890,
    rating: 4.7,
    expertise: ["SEO", "Google Ads", "Analytics", "Content Marketing"],
    social: { linkedin: "#", twitter: "#" }
  },
  {
    id: 4,
    name: "Zeynep Arslan",
    title: "Lead UX/UI Designer",
    bio: "Apple ve Spotify'da tasarımcı olarak çalışmış, kullanıcı odaklı tasarım konusunda uluslararası ödüllü tasarımcı.",
    avatar: "ZA",
    courses: 3,
    students: 1560,
    rating: 4.9,
    expertise: ["Figma", "UX Research", "Design Systems", "Prototyping"],
    social: { linkedin: "#", twitter: "#", github: "#" }
  },
  {
    id: 5,
    name: "Can Özkan",
    title: "Mobil Uygulama Geliştirici",
    bio: "React Native ve Flutter konusunda uzmanlaşmış, 50'den fazla mobil uygulama geliştirmiş deneyimli yazılımcı.",
    avatar: "CÖ",
    courses: 4,
    students: 2230,
    rating: 4.6,
    expertise: ["React Native", "Flutter", "Swift", "Kotlin"],
    social: { linkedin: "#", github: "#" }
  },
  {
    id: 6,
    name: "Burak Şahin",
    title: "DevOps & Güvenlik Uzmanı",
    bio: "Büyük ölçekli sistemlerde 12 yıl deneyim. AWS ve Azure sertifikalı, siber güvenlik konusunda eğitmen.",
    avatar: "BŞ",
    courses: 3,
    students: 1740,
    rating: 4.8,
    expertise: ["Docker", "Kubernetes", "AWS", "Penetration Testing"],
    social: { linkedin: "#", twitter: "#", github: "#" }
  }
];

const TESTIMONIALS = [
  {
    text: "GUZEM sayesinde kariyerimde büyük bir sıçrama yaptım. Python ile Veri Bilimi eğitimi gerçekten muhteşemdi. Şimdi bir teknoloji şirketinde veri analisti olarak çalışıyorum.",
    name: "Ayşe Demir",
    role: "Veri Analisti, TechCorp",
    avatar: "AD"
  },
  {
    text: "Web geliştirme eğitimini tamamladıktan sonra kendi freelance işimi kurdum. Eğitmenler çok bilgili ve içerikler güncel. Kesinlikle tavsiye ediyorum!",
    name: "Murat Yıldız",
    role: "Freelance Developer",
    avatar: "MY"
  },
  {
    text: "Dijital pazarlama eğitimi kariyer değişikliği yapmamda çok etkili oldu. Pratik odaklı eğitim yaklaşımı gerçekten fark yaratıyor.",
    name: "Selin Ak",
    role: "Dijital Pazarlama Uzmanı",
    avatar: "SA"
  }
];

const CATEGORIES = ["Tümü", "Veri Bilimi", "Web Geliştirme", "Pazarlama", "Tasarım", "Yapay Zeka", "Mobil", "Güvenlik", "DevOps"];
