//Init global vars
let canvasNail = document.getElementById("canvas1");
let ctxNail = canvasNail.getContext("2d");
let downscaleResolution = 1000;
let canvasDownscale = document.getElementById("canvas2");
let ctxDownscale = canvasDownscale.getContext("2d");
let canvasPreview = document.getElementById("canvas3");
let ctxPreview = canvasPreview.getContext("2d");
let canvasCompute = document.getElementById("canvas4");
let ctxCompute = canvasCompute.getContext("2d");
//Init cache vars
let quickCurGrayscaleCache = new Uint8Array(canvasCompute.width*canvasCompute.height); //Load this up before computing stuff to avoid having to getImageData over and over
let quickTargetGrayscaleCache = new Uint8Array(canvasDownscale.width*canvasDownscale.height); //Load this up before computing stuff to avoid having to getImageData over and over
//
const calculatedStringSet = new Set();
let isDoneAddingStrings = false;
// Used in distancePointToLine();
let DISTANCE_downscaleToNailSpace = 0, DISTANCE_nailToDownscaleSpace = 0;
//
let NailInfoObject = {
    nailLocArr: [], //Array with elements representing locations of nails
    nailNumArr: [], //Array with elements representing order of traversal of nails
    nailStringsPixelComputedInfluenceMap: {} //Map with elements representing which pixels a string from x->y alter by how much
};
// Used in nchoosek();
var logf;
if (true) { //This is done so we can hide this monstrocity.
    logf = [0,0,0.6931471805599453,1.791759469228055,3.1780538303479453,4.787491742782046,6.579251212010101,8.525161361065415,10.60460290274525,12.80182748008147,15.104412573075518,17.502307845873887,19.98721449566189,22.552163853123425,25.191221182738683,27.899271383840894,30.671860106080675,33.50507345013689,36.39544520803305,39.339884187199495,42.335616460753485,45.38013889847691,48.47118135183523,51.60667556776438,54.784729398112326,58.003605222980525,61.26170176100201,64.55753862700634,67.88974313718154,71.25703896716801,74.65823634883017,78.09222355331532,81.55795945611504,85.05446701758153,88.5808275421977,92.13617560368711,95.71969454214322,99.33061245478744,102.96819861451382,106.63176026064347,110.3206397147574,114.0342117814617,117.77188139974507,121.53308151543864,125.3172711493569,129.12393363912722,132.95257503561632,136.80272263732638,140.67392364823428,144.5657439463449,148.47776695177305,152.40959258449737,156.3608363030788,160.33112821663093,164.3201122631952,168.32744544842768,172.35279713916282,176.39584840699737,180.45629141754378,184.5338288614495,188.6281734236716,192.7390472878449,196.86618167288998,201.00931639928152,205.1681994826412,209.34258675253685,213.53224149456327,217.73693411395422,221.95644181913033,226.1905483237276,230.43904356577696,234.70172344281826,238.97838956183432,243.2688490029827,247.57291409618688,251.8904022097232,256.22113555000954,260.5649409718632,264.92164979855283,269.29109765101987,273.67312428569375,278.0675734403662,282.47429268763045,286.89313329542705,291.32395009427034,295.76660135076065,300.22094864701415,304.6868567656687,309.16419358014696,313.6528299498791,318.15263962020936,322.6634991267262,327.18528770377526,331.71788719692853,336.2611819791985,340.8150588707991,345.3794070622669,349.9541180407703,354.5390855194409,359.1342053695755,363.7393755555636,368.35449607240486,372.97946888568913,377.6141978739188,382.2585887730602,386.91254912321773,391.5759882173298,396.24881705179166,400.9309482789159,405.622296161145,410.32277652693745,415.0323067282498,419.7508055995449,424.4781934182572,429.2143918666517,433.9593239950149,438.7129141861213,443.47508812091905,448.2457727453847,453.02489623849624,457.8123879812783,462.608178526875,467.41219957160826,472.2243839269807,477.04466549258575,481.8729792298881,486.70926113683953,491.5534482232981,496.40547848721775,501.2652908915794,506.132825342035,511.0080226652362,515.8908245878225,520.7811737160442,525.6790135159952,530.5842882944336,535.4969431801696,540.4169241059977,545.344177791155,550.2786517242856,555.220294146895,560.1690540372731,565.1248810948744,570.0877257251343,575.0575390247103,580.0342727671309,585.0178793888392,590.008311975618,595.0055242493821,600.0094705553275,605.0201058494238,610.0373856862387,615.061266207085,620.0917041284775,625.1286567308912,630.1720818478104,635.22193785506,640.2781836604083,645.3407786934353,650.4096828956555,655.4848567108893,660.5662610758737,665.6538574111062,670.7476076119129,675.8474740397371,680.9534195136376,686.0654073019941,691.1834011144109,696.3073650938142,701.4372638087373,706.5730622457876,711.7147258022902,716.8622202791037,722.0155118736014,727.174567172816,732.3393531467394,737.5098371417776,742.6859868743513,747.8677704246434,753.0551562304842,758.2481130813744,763.4466101126402,768.650616799717,773.8601029525585,779.0750387101674,784.2953945352457,789.5211412089589,794.7522498258135,799.9886917886435,805.2304388037031,810.4774628758636,815.7297363039102,820.9872316759379,826.2499218648428,831.5177800239061,836.7907795824698,842.0688942417003,847.3520979704383,852.6403650011329,857.9336698258574,863.2319871924054,868.5352921004645,873.8435597978657,879.1567657769075,884.4748857707517,889.7978957498901,895.1257719186797,900.458490711945,905.7960287916463,911.1383630436111,916.4854705743286,921.8373287078047,927.1939149824767,932.5552071481861,937.921183163208,943.2918211913357,948.6670995990198,954.0469969525603,959.4314920153495,964.8205637451659,970.2141912915183,975.6123539930361,981.0150313749084,986.4222031463685,991.8338491982236,997.249949600428,1002.6704845997002,1008.0954346171816,1013.5247802461361,1018.9585022496904,1024.3965815586137,1029.8389992691355,1035.2857366408018,1040.7367750943674,1046.1920962097252,1051.6516817238694,1057.115513528895,1062.58357367003,1068.0558443437017,1073.5323078956333,1079.0129468189753,1084.4977437524658,1089.9866814786226,1095.4797429219632,1100.9769111472565,1106.478169357801,1111.9835008937334,1117.4928892303615,1123.0063179765264,1128.523770872991,1134.0452317908532,1139.570684729985,1145.1001138174965,1150.6335033062242,1156.1708375732428,1161.7121011184013,1167.2572785628809,1172.806354647776,1178.3593142326977,1183.916142294397,1189.4768239254126,1195.0413443327354,1200.6096888364966,1206.1818428686745,1211.7577919718208,1217.337521797807,1222.9210181065887,1228.508266764989,1234.0992537455,1239.6939651251018,1245.2923870841003,1250.89450590498,1256.500307971276,1262.109779766461,1267.7229078728492,1273.3396789705157,1278.9600798362328,1284.5840973424201,1290.2117184561107,1295.842930237932,1301.4777198411014,1307.116074510435,1312.757981581373,1318.4034284790164,1324.0524027171775,1329.7048918974463,1335.360883708266,1341.0203659240258,1346.6833264041618,1352.349753092274,1358.0196340152547,1363.6929572824263,1369.3697110846945,1375.0498836937115,1380.7334634610502,1386.42043881739,1392.1107982717142,1397.804530410517,1403.5016238970222,1409.2020674704129,1414.9058499450691,1420.612960209818,1426.323387227193,1432.0371200327024,1437.7541477341088,1443.474459510716,1449.1980446126686,1454.9248923602559,1460.6549921432295,1466.3883334201273,1472.1249057176065,1477.8646986297856,1483.6077018175952,1489.3539050081354,1495.1032979940437,1500.8558706328693,1506.6116128464562,1512.3705146203336,1518.1325660031137,1523.8977571058986,1529.6660781016924,1535.4375192248224,1541.2120707703668,1546.9897230935894,1552.7704666093819,1558.5542917917116,1564.3411891730784,1570.1311493439757,1575.92416295236,1581.7202207031253,1587.5193133575858,1593.321431732963,1599.1265667018795,1604.9347091918598,1610.7458501848366,1616.5599807166616,1622.3770918766247,1628.197174806977,1634.0202207024602,1639.8462208098406,1645.675166427451,1651.5070489047343,1657.3418596417969,1663.1795900889629,1669.0202317463363,1674.8637761633677,1680.7102149384255,1686.5595397183722,1692.4117421981466,1698.266814120349,1704.1247472748323,1709.985533498298,1715.8491646738962,1721.7156327308296,1727.5849296439633,1733.4570474334387,1739.3319781642906,1745.2097139460702,1751.090246932471,1756.9735693209593,1762.8596733524096,1768.7485513107424,1774.640195522568,1780.534598356833,1786.4317522244696,1792.331649578052,1798.2342829114534,1804.139644759508,1810.047727697677,1815.9585243417175,1821.8720273473557,1827.7882294099632,1833.7071232642363,1839.62870168388,1845.5529574812947,1851.4798835072652,1857.409472650655,1863.341717838103,1869.2766120337226,1875.214148238805,1881.1543194915253,1887.097118866652,1893.0425394752585,1898.990574464439,1904.9412170170267,1910.8944603513146,1916.8502977207795,1922.8087224138094,1928.7697277534326,1934.733307097051,1940.6994538361746,1946.66816139616,1952.6394232359505,1958.6132328478197,1964.5895837571177,1970.5684695220189,1976.5498837332734,1982.5338200139606,1988.520272019245,1994.509233436135,2000.500697983243,2006.4946594105497,2012.4911114991687,2018.4900480611154,2024.4914629390767,2030.4953500061831,2036.5017031657849,2042.5105163512276,2048.5217835256317,2054.5354986816747,2060.551655841373,2066.570249055869,2072.5912724052187,2078.6147199981797,2084.640585972005,2090.6688644922356,2096.6995497524968,2102.7326359742956,2108.7681174068202,2114.8059883267424,2120.84624303802,2126.8888758717026,2132.9338811857388,2138.981253364785,2145.030986820017,2151.0830759889413,2157.1375153352105,2163.194299348439,2169.253422544021,2175.3148794629487,2181.378664671636,2187.44477276174,2193.513198349984,2199.5839360779864,2205.656980612087,2211.732326643176,2217.809968886525,2223.8899020816207,2229.972120991997,2236.0566204050724,2242.1433951319846,2248.2324400074313,2254.323749889509,2260.4173196595543,2266.5131442219867,2272.611218504153,2278.711537456173,2284.8140960507867,2290.9188892832017,2297.025912170944,2303.1351597537086,2309.2466270932114,2315.3603092730436,2321.476201398527,2327.594298596568,2333.714596015519,2339.8370888250333,2345.9617722159273,2352.0886414000415,2358.217691610102,2364.3489180995853,2370.482316142582,2376.6178810336637,2382.75560808775,2388.895492639976,2395.0375300455635,2401.181715679689,2407.3280449373583,2413.476513233276,2419.6271160017222,2425.779848696426,2431.9347067904428,2438.0916857760285,2444.2507811645205,2450.4119884862157,2456.5753032902503,2462.7407211444815,2468.90823763537,2475.0778483678614,2481.2495489652724,2487.4233350691743,2493.59920233928,2499.7771464533307,2505.9571631069834,2512.1392480137,2518.323396904638,2524.5096055285385,2530.697869651621,2536.8881850574744,2543.080547546949,2549.274952938054,2555.4713970658486,2561.669875782341,2567.8703849563835,2574.0729204735712,2580.27747823614,2586.4840541628646,2592.6926441889614,2598.903244265986,2605.1158503617376,2611.3304584601597,2617.5470645612445,2623.7656646809364,2629.986254851036,2636.2088311191073,2642.433389548383,2648.65992621767,2654.8884372212615,2661.11891866884,2667.3513666853905,2673.585777411109,2679.8221470013127,2686.0604716263524,2692.300747471523,2698.5429707369785,2704.7871376376424,2711.033244403124,2717.2812872776326,2723.531262519892,2729.783166403058,2736.036995214634,2742.292745256387,2748.55041284427,2754.809994308335,2761.0714859926557,2767.3348842552473,2773.600185467985,2779.8673860165263,2786.1364823002327,2792.407470732091,2798.680347738637,2804.9551097598787,2811.23175324922,2817.510274673386,2823.790670512346,2830.072937259242,2836.3570714203124,2842.6430695148215,2848.930928074983,2855.220643645892,2861.51221278545,2867.805632064297,2874.1008980657366,2880.3980073856706,2886.6969566325265,2892.9977424271897,2899.3003614029344,2905.6048102053564,2911.9110854923047,2918.2191839338143,2924.529102212041,2930.8408370211937,2937.1543850674707,2943.469743068993,2949.7869077557402,2956.1058758694867,2962.4266441637374,2968.7492094036647,2975.073568366046,2981.399717839201,2987.72765462293,2994.0573755284527,3000.3888773783465,3006.722157006486,3013.0572112579844,3019.3940369891307,3025.732631067334,3032.0729903710617,3038.4151117897827,3044.758992223909,3051.1046285847374,3057.452017794393,3063.801156785773,3070.152042502488,3076.5046718988074,3082.8590419396046,3089.2151496003003,3095.5729918668085,3101.9325657354807,3108.293868213054,3114.656896316594,3121.0216470734463,3127.3881175211777,3133.756304707528,3140.1262056903565,3146.497817537588,3152.8711373271653,3159.2461621469934,3165.622889094892,3172.0013152785436,3178.3814378154434,3184.7632538328494,3191.146760467733,3197.5319548667308,3203.9188341860936,3210.3073955916393,3216.6976362587047,3223.0895533720973,3229.483144126048,3235.8784057241637,3242.27533537938,3248.673930313915,3255.074187759224,3261.476104955951,3267.8796791538857,3274.2849076119164,3280.6917875979857,3287.100316389045,3293.5104912710112,3299.922309538721,3306.3357684958883,3312.7508654550597,3319.167597737572,3325.585962673508,3332.005957601655,3338.4275798694616,3344.850826832995,3351.2756958569007,3357.7021843143584,3364.130289587043,3370.5600090650823,3376.9913401470158,3383.424280239755,3389.8588267585424,3396.294977126912,3402.7327287766484,3409.1720791477487,3415.6130256883816,3422.0555658548496,3428.49969711155,3434.9454169309356,3441.392722793477,3447.841612187624,3454.292082609768,3460.744131564205,3467.1977565630978,3473.652955126438,3480.1097247820103,3486.568063065355,3493.0279675197326,3499.4894356960863,3505.952465153007,3512.417053456697,3518.8831981809344,3525.350896907039,3531.8201472238347,3538.290946727617,3544.763293022118,3551.23718371847,3557.7126164351744,3564.189588798064,3570.668098440273,3577.1481430021995,3583.629720131476,3590.1128274829334,3596.5974627185687,3603.083623507513,3609.571307525998,3616.060512457323,3622.5512359918257,3629.043475826846,3635.537229666698,3642.0324952226347,3648.5292702128204,3655.027552362297,3661.5273394029527,3668.028629073493,3674.531419119409,3681.0357072929455,3687.5414913530735,3694.0487690654586,3700.5575382024304,3707.0677965429536,3713.5795418725984,3720.092771983511,3726.607484674383,3733.123677750426,3739.641349023338,3746.1604963112786,3752.681117438837,3759.203210237007,3765.7267725431566,3772.251802201,3778.778297060571,3785.3062549781935,3791.8356738164557,3798.3665514441814,3804.898885736404,3811.4326745743374,3817.967915845351,3824.5046074429424,3831.04274726671,3837.582333222328,3844.123363221518,3850.6658351820247,3857.2097470275894,3863.755096687924,3870.3018820986845,3876.8501012014467,3883.3997519436807,3889.950832278724,3896.503340165759,3903.0572735697847,3909.6126304615955,3916.1694088177537,3922.727606620566,3929.287221858059,3935.8482525239556,3942.410696617649,3948.9745521441814,3955.539817114217,3962.10648954402,3968.6745674554318,3975.244048875846,3981.8149318381857,3988.38721438088,3994.9608945478403,4001.53597038844,4008.1124399574883,4014.690301315209,4021.2695525272193,4027.850191664504,4034.432216803397,4041.015626025556,4047.6004174179416,4054.1865890727963,4060.7741390876213,4067.363065565155,4073.9533666133516,4080.54504034536,4087.1380848795025,4093.7324983392523,4100.328278853213,4106.9254245551,4113.523933583715,4120.123804082928,4126.725034201657,4133.327622093846,4139.931565918447,4146.536863839395,4153.143514025593,4159.751514650889,4166.360863894056,4172.971559938774,4179.583600973607,4186.196985191987,4192.811710792191,4199.427775977324,4206.045178955298,4212.663917938815,4219.283991145345,4225.905396797109,4232.528133121058,4239.152198348858,4245.777590716866,4252.404308466115,4259.032349842295,4265.661713095732,4272.292396481374,4278.92439825877,4285.557716692051,4292.192350049912,4298.8282966055995,4305.465554636884,4312.104122426051,4318.743998259877,4325.385180429617,4332.027667230985,4338.671456964133,4345.3165479336385,4351.962938448486,4358.610626822049,4365.259611372074,4371.909890420661,4378.561462294251,4385.2143253236045,4391.868477843787,4398.523918194155,4405.1806447183335,4411.838655764204,4418.497949683888,4425.158524833728,4431.820379574273,4438.483512270263,4445.147921290614,4451.813605008397,4458.480561800826,4465.148790049243,4471.818288139101,4478.4890544599475,4485.161087405409,4491.834385373176,4498.5089467649905,4505.184769986626,4511.861853447873,4518.5401955625275,4525.219794748372,4531.9006494271625,4538.582758024612,4545.266118970379,4551.950730698047,4558.636591645115,4565.323700252981,4572.012054966928,4578.701654236107,4585.392496513526,4592.0845802560325,4598.777903924302,4605.472465982823,4612.168264899882,4618.865299147548,4625.563567201663,4632.263067541825,4638.963798651373,4645.665759017375,4652.368947130616,4659.07336148558,4665.77900058044,4672.485862917043,4679.1939470008965,4685.903251341155,4692.613774450608,4699.325514845664,4706.03847104634,4712.7526415762495,4719.468024962584,4726.184619736106,4732.9024244311295,4739.621437585514,4746.34165774065,4753.0630834414405,4759.785713236296,4766.509545677117,4773.2345793192835,4779.9608127216425,4786.688244446494,4793.416873059578,4800.146697130068,4806.87771523055,4813.609925937018,4820.343327828855,4827.077919488828,4833.813699503071,4840.550666461073,4847.288818955669,4854.028155583026,4860.768674942632,4867.510375637284,4874.253256273076,4880.997315459387,4887.742551808871,4894.488963937444,4901.236550464274,4907.985310011765,4914.735241205553,4921.4863426744905,4928.238613050632,4934.99205096923,4941.746655068718,4948.502423990702,4955.259356379949,4962.017450884377,4968.77670615504,4975.537120846124,4982.298693614928,4989.06142312186,4995.825308030422,5002.590347007203,5009.356538721863,5016.123881847128,5022.892375058777,5029.66201703563,5036.432806459539,5043.2047420153785,5049.977822391034,5056.752046277392,5063.527412368328,5070.303919360701,5077.081565954336,5083.860350852021,5090.640272759493,5097.421330385429,5104.203522441436,5110.98684764204,5117.771304704677,5124.556892349685,5131.34360930029,5138.131454282599,5144.920426025592,5151.710523261106,5158.5017447238315,5165.294089151303,5172.0875552838825,5178.8821418647585,5185.677847639932,5192.474671358207,5199.272611771182,5206.07166763324,5212.871837701543,5219.673120736014,5226.475515499339,5233.279020756948,5240.08363527701,5246.889357830427,5253.696187190819,5260.504122134519,5267.313161440562,5274.123303890678,5280.934548269279,5287.746893363456,5294.560337962967,5301.374880860228,5308.190520850302,5315.007256730897,5321.825087302352,5328.644011367627,5335.4640277323015,5342.285135204558,5349.107332595178,5355.930618717534,5362.754992387578,5369.580452423833,5376.406997647389,5383.234626881892,5390.063338953533,5396.893132691046,5403.724006925692,5410.555960491258,5417.388992224044,5424.223100962858,5431.058285549005,5437.894544826282,5444.7318776409675,5451.570282841815,5458.409759280044,5465.250305809333,5472.091921285811,5478.9346045680495,5485.778354517056,5492.623169996265,5499.469049871529,5506.315993011114,5513.1639982856905,5520.013064568324,5526.86319073447,5533.7143756619635,5540.566618231015,5547.419917324201,5554.274271826456,5561.129680625067,5567.986142609661,5574.843656672207,5581.702221706998,5588.561836610652,5595.4225002821,5602.284211622581,5609.146969535633,5616.010772927086,5622.8756207050565,5629.74151177994,5636.608445064402,5643.4764194733725,5650.345433924038,5657.215487335836,5664.086578630447,5670.958706731785,5677.831870565998,5684.706069061451,5691.581301148727,5698.457565760618,5705.334861832116,5712.213188300408,5719.0925441048685,5725.972928187054,5732.8543394906965,5739.736776961694,5746.620239548107,5753.50472620015,5760.390235870184,5767.276767512715,5774.16432008438,5781.052892543946,5787.9424838523,5794.833092972447,5801.724718869499,5808.6173605106715,5815.5110168652745,5822.405686904708,5829.301369602456,5836.198063934079,5843.095768877208,5849.994483411538,5856.894206518822,5863.794937182867,5870.696674389524,5877.599417126682,5884.503164384267,5891.407915154228,5898.31366843054,5905.220423209189,5912.128178488171];
}
// Used in computeEveryStringINfluenceMap()
let COMPUTE_bufferLength;
let COMPUTE_influenceArr;
let COMPUTE_parentNailLoc, COMPUTE_childNailLoc, COMPUTE_ID = "0,1", COMPUTE_index = 0, COMPUTE_distance = 0;
/*
* @ 300 res
*  255 draws too little
*  100 draws better, but still too little
*  60 draws better, but a tad too little (pretty good with stroke width 2)
*  50
*  30 draws good, but mildly too much (pretty good with stroke width 2)
*  10 draws too much
*/
let FullStrengthPixelInfluencePerString = 40;
let FullStrengthPixelInfluencePerStringMult = FullStrengthPixelInfluencePerString/100;
function setFullStrengthPixelInfluencePerString(value) {
    FullStrengthPixelInfluencePerString = value;
    FullStrengthPixelInfluencePerStringMult = FullStrengthPixelInfluencePerString/100;
}
let PixelInfluenceCurve = {
    distanceArr:  [0, 1, 2], //Distance in pixels
    influenceArr: [100, 50, 0]  //Influence from 0 (empty) -> 100 (filled)
    // distanceArr:  [0, 1, 2, 3, 4, 5, 6], //Distance in pixels
    // influenceArr: [1*FullStrengthPixelInfluencePerString, .5*FullStrengthPixelInfluencePerString, .25*FullStrengthPixelInfluencePerString, .12*FullStrengthPixelInfluencePerString, .06*FullStrengthPixelInfluencePerString, .03*FullStrengthPixelInfluencePerString, 0]  //Influence from 0 (empty) -> FullStrengthPixelInfluencePerString (filled)
};
let PixelGrayscaleDataArr = [];
//Do initialization if necessary
    regenerateNailCanvas(900,900);
    wipeNailCanvas();
    regenerateDownscaleCanvas(900,900,true);
    //F5 persistence stuff
    canvasDownscale.style.opacity = downscalealphaslider.value/100;
    regenerateDownscaleCanvas((downscalewidthinput.value || downscaleResolution),(downscaleheightinput.value || downscaleResolution),false);
    // canvasDownscale.width = downscalewidthinput.value || downscaleResolution;
    // canvasDownscale.height = downscaleheightinput.value || downscaleResolution;
    canvasCompute.width = downscalewidthinput.value || downscaleResolution;
    canvasCompute.height = downscaleheightinput.value || downscaleResolution;
    downloadinfluencemapbutton.disabled = true;
    //This comes last bc resizing undoes what it does
    resetStringAnalysis();



function regenerateNailCanvas(idealWidth, idealHeight) {
    let idealAspectRatio = idealWidth/idealHeight;
    //Keep original aspect ratio, but resize canvas pixels so largest is 1920
    // This is necessary because even pixellated images need to be simulated
    // w/ higher resolution "string"
    let MAXCANVASDIMENSION = 1920;
    let targetCanvasHeight, targetCanvasWidth;
    if (idealWidth <= idealHeight) {
        targetCanvasHeight = Math.max(MAXCANVASDIMENSION, idealHeight);
        targetCanvasWidth = targetCanvasHeight * idealAspectRatio;
    } else {
        targetCanvasWidth = Math.max(MAXCANVASDIMENSION, idealWidth);
        targetCanvasHeight = targetCanvasWidth / idealAspectRatio;
    }
    canvasNail.width = targetCanvasWidth;
    canvasNail.height = targetCanvasHeight;
    //Keep original aspect ratio, but resize to visibly fit within 900x900
    let MAXVIEWDIMENSION = 900;
    let targetViewHeight, targetViewWidth;
    if (idealWidth <= idealHeight) {
        targetViewHeight = Math.max(MAXVIEWDIMENSION, idealHeight);
        targetViewWidth = targetViewHeight/idealHeight * idealWidth;
    } else {
        targetViewWidth = Math.max(MAXVIEWDIMENSION, idealWidth);
        targetViewHeight = targetViewWidth/idealWidth * idealHeight;
    }
    canvasNail.style.width = targetViewWidth + "px";
    canvasNail.style.height = targetViewHeight + "px";
    //
    DISTANCE_downscaleToNailSpace = canvasNail.width/canvasDownscale.width;
    DISTANCE_nailToDownscaleSpace = canvasDownscale.width/canvasNail.width;
}
function regenerateDownscaleCanvas(idealWidth, idealHeight, doIncludeStyleSize) {
    let idealAspectRatio = idealWidth/idealHeight;
    //Keep original aspect ratio, but resize canvas pixels so largest is
    // downscaled.
    let MAXCANVASDIMENSION = downscaleResolution;
    let targetCanvasHeight, targetCanvasWidth;
    if (idealWidth <= idealHeight) {
        targetCanvasHeight = idealHeight > MAXCANVASDIMENSION ? MAXCANVASDIMENSION : idealHeight;
        targetCanvasWidth = targetCanvasHeight * idealAspectRatio;
    } else {
        targetCanvasWidth = idealWidth > MAXCANVASDIMENSION ? MAXCANVASDIMENSION : idealWidth;
        targetCanvasHeight = targetCanvasWidth / idealAspectRatio;
    }
    canvasDownscale.width = targetCanvasWidth;
    canvasDownscale.height = targetCanvasHeight;
    canvasCompute.width = targetCanvasWidth;
    canvasCompute.height = targetCanvasHeight;
    if (doIncludeStyleSize) {
        //Keep original aspect ratio, but resize to visibly fit within 900x900
        let MAXVIEWDIMENSION = 900;
        let targetViewHeight, targetViewWidth;
        if (idealWidth <= idealHeight) {
            targetViewHeight = idealHeight > MAXVIEWDIMENSION ? MAXVIEWDIMENSION : idealHeight;
            targetViewWidth = targetViewHeight/idealHeight * idealWidth;
        } else {
            targetViewWidth = idealWidth > MAXVIEWDIMENSION ? MAXVIEWDIMENSION : idealWidth;
            targetViewHeight = targetViewWidth/idealWidth * idealHeight;
        }
        canvasDownscale.style.width = targetViewWidth + "px";
        canvasDownscale.style.height = targetViewHeight + "px";
        // canvasCompute.style.width = targetViewWidth + "px";
        // canvasCompute.style.height = targetViewHeight + "px";
    }
    //
    quickCurGrayscaleCache = new Uint8Array(canvasCompute.width*canvasCompute.height); //Load this up before computing stuff to avoid having to getImageData over and over
    quickTargetGrayscaleCache = new Uint8Array(canvasDownscale.width*canvasDownscale.height); //Load this up before computing stuff to avoid having to getImageData over and over
    DISTANCE_downscaleToNailSpace = canvasNail.width/canvasDownscale.width;
    DISTANCE_nailToDownscaleSpace = canvasDownscale.width/canvasNail.width;
    COMPUTE_bufferLength = canvasDownscale.width * canvasDownscale.height;
    COMPUTE_influenceArr = new Uint16Array(COMPUTE_bufferLength * Math.round(nchoosek(NailInfoObject.nailLocArr.length, 2)));
    //
    resetStringAnalysis();
}
function getRadioGroupOption(groupName) {
    let elems = document.getElementsByName(groupName);
    for (i = 0; i < elems.length; i++) {
        if (elems[i].checked)
            return elems[i].value;
    }
    return "";
}
function outputGen(string) {
    let elem = document.getElementById("genoutputtextarea");
    elem.value = string;
}
function generateNails() {
    let type = getRadioGroupOption("nailgentype");
    let nailNum = parseInt(document.getElementById("gennailnum").value);
    if ((type != "circle" && type != "rectangle") || !nailNum)
        return;

    if (type == "circle") {
        let curTheta = 0;
        let deltaTheta = 360 / nailNum;
        let nailArr = [];
        let iterationLimit = 10000;
        let smallerSize = canvasNail.width < canvasNail.height ? canvasNail.width / 2 : canvasNail.height / 2;
        while (curTheta <= 360 && iterationLimit-- > 0) {
            let curThetaRad = curTheta * Math.PI / 180;
            let x = Math.cos(curThetaRad) * (smallerSize) + (canvasNail.width / 2);
            x = Math.round(x * 10000) / 10000 //Round to 4 decimal places to look pretty
            let y = Math.sin(curThetaRad) * (smallerSize) + (canvasNail.height / 2);
            y = Math.round(y * 10000) / 10000 //Round to 4 decimal places to look pretty
            nailArr.push(x + "," + y);
            curTheta += deltaTheta;
        }
        if (nailArr[0].x == nailArr[nailArr.length-1].x && nailArr[0].y == nailArr[nailArr.length-1].y)
            nailArr.splice(nailArr.length-1,1);
        outputGen(JSON.stringify(nailArr));
    } else if (type == "rectangle") {
        //TODO
    } else {
        console.log("Generator Type Error");
    }
}
function wipeNailCanvas() {
    let cacheFillStyle = ctxNail.fillStyle || "black";
    ctxNail.fillStyle = "white";
    ctxNail.fillRect(0,0,canvasNail.width,canvasNail.height);
    //
    let cacheStrokeStyle = ctxNail.strokeStyle || "black";
    ctxNail.strokeStyle = "red";
    ctxNail.beginPath();
    let smallerSize = canvasNail.width < canvasNail.height ? canvasNail.width / 2 : canvasNail.height / 2;
    ctxNail.ellipse(canvasNail.width / 2, canvasNail.height / 2, smallerSize, smallerSize, 0, 0, Math.PI*2);
    ctxNail.rect(0, 0, canvasNail.width, canvasNail.height);
    ctxNail.stroke();
    //
    ctxNail.fillStyle = cacheFillStyle;
    ctxNail.strokeStyle = cacheStrokeStyle;
}

function distancePointToLine(px, py, x1, y1, x2, y2) {
    // I could have done this myself but I got this from ChatGPT for sake of time
    // Also I had to modify it because it didn't account for horizontal slopes

    if (x1 == x2 && y1 == y2)
        return null;

    // px and px are in Downscale space, we must convert to Nail space
    px *= DISTANCE_downscaleToNailSpace;
    py *= DISTANCE_downscaleToNailSpace;

    if (y1 == y2) {
        // console.log("case 1");
        let distance = Math.abs(py - y1);
        // console.log("m=0: " + distance);
        // Distance is in Nail space, we must convert to Downscale space
        distance *= DISTANCE_nailToDownscaleSpace;
        return distance;
    } else if (x1 == x2) {
        // console.log("case 2");
        let distance = Math.abs(px - x1);
        // console.log("m=inf: " + distance);
        // Distance is in Nail space, we must convert to Downscale space
        distance *= DISTANCE_nailToDownscaleSpace;
        return distance;
    }

    //Implement Bounding Box Optimization Conditions
    const minX = Math.min(x1, x2);
    const minY = Math.min(y1, y2);
    const maxX = Math.max(x1, x2);
    const maxY = Math.max(y1, y2);
    if (px <= maxX && px >= minX && py <= maxY && py >= minY) { //If is in bounding box
        // console.log("case 3");
        // Step 1: Calculate the equation of the line (y = mx + c)
        let m = (y2 - y1) / (x2 - x1); // Slope
        const c = y1 - m * x1; // Y-intercept
        // Step 2: Find the perpendicular line passing through the point (px, py)
        const mPerpendicular = -1 / m; // Perpendicular line has a negative reciprocal slope
        const cPerpendicular = py - mPerpendicular * px;
        // Step 3: Calculate the intersection point of the perpendicular line and the original line
        const intersectionX = (cPerpendicular - c) / (m - mPerpendicular);
        const intersectionY = m * intersectionX + c;
        // Step 4: Calculate the distance between the point (px, py) and the intersection point
        let distance = Math.sqrt((px - intersectionX) ** 2 + (py - intersectionY) ** 2);
        // Step 5: Distance is in Nail space, we must convert to Downscale space
        distance *= DISTANCE_nailToDownscaleSpace;
        return distance;
    } else { //If not in bounding box
        // console.log("case 4");
        let distanceFromP1 = Math.abs(px - x1) + Math.abs(py - y1);
        let distanceFromP2 = Math.abs(px - x2) + Math.abs(py - y2);
        let distance = (distanceFromP1 < distanceFromP2) ? distanceFromP1 : distanceFromP2;
        distance *= DISTANCE_nailToDownscaleSpace; //Convert to downscale space from nail space
        return distance;
    }

    return null;
}
function distanceToInfluence(distance) {
    if (distance > PixelInfluenceCurve.distanceArr[PixelInfluenceCurve.distanceArr.length-1])
        return 0;

    // let cacheMultip = FullStrengthPixelInfluencePerString/100;
    for (let i = PixelInfluenceCurve.distanceArr.length-1;i >= 0;i--) {
        if (distance >= PixelInfluenceCurve.distanceArr[i]) {
            return PixelInfluenceCurve.influenceArr[i];
            // return PixelInfluenceCurve.influenceArr[i]*cacheMultip;
        }
    }
}
function drawNails() {
    wipeNailCanvas();
    ctxNail.fillStyle = "black";
    for (let nailLoc of NailInfoObject.nailLocArr) {
        ctxNail.beginPath();
        ctxNail.ellipse(nailLoc.x, nailLoc.y, 5, 5, 0, 0, 2*Math.PI);
        ctxNail.fill();
    }
}
function loadNails() {
    let nailArr = JSON.parse(document.getElementById("loadtextarea").value);
    if (!nailArr)
        return;

    let nailLocArr = [];
    for (let encodedLoc of nailArr) {
        let nailLoc = {};
        let splitIndex = encodedLoc.indexOf(",");
        nailLoc.x = parseFloat(encodedLoc.substring(0, splitIndex));
        nailLoc.y = parseFloat(encodedLoc.substring(splitIndex+1, encodedLoc.length));
        nailLocArr.push(nailLoc);
    }
    NailInfoObject.nailLocArr = nailLocArr;

    drawNails();
    COMPUTE_influenceArr = new Uint16Array(COMPUTE_bufferLength * Math.round(nchoosek(NailInfoObject.nailLocArr.length, 2)));
    resetStringAnalysis();
}
function loadRandomSimulationCanvas() {
    for (let y = 0;y < canvasDownscale.height;y++) {
        for (let x = 0;x < canvasDownscale.width;x++) {
            let grayscale = Math.random() * 255;
            ctxDownscale.fillStyle = "rgb(" + grayscale + "," + grayscale + "," + grayscale + ")";
            ctxDownscale.fillRect(x,y,1,1);
        }
    }
}


function invertPreview() {
    const data = ctxPreview.getImageData(0,0,canvasPreview.width,canvasPreview.height).data;
    let dataIndex = 0;
    for (let y = 0;y < canvasPreview.height;y++) {
        for (let x = 0;x < canvasPreview.width;x++,dataIndex+=4) {
            let curGrayscale = (data[dataIndex] + data[dataIndex+1] + data[dataIndex+2])/3;
            let grayscaleValue;
            if (data[dataIndex+3] == 0)
                grayscaleValue = 0;
            else
                grayscaleValue = 255 - (curGrayscale*data[dataIndex]); //255 - (value * alpha)
            ctxPreview.fillStyle = "rgb(" + grayscaleValue + "," + grayscaleValue + "," + grayscaleValue + ")";
            ctxPreview.fillRect(x,y,1,1);
        }
    }
}
function loadPreviewIntoDownscale(ifUseAdvancedResize) {
    if (!ifUseAdvancedResize)
        ctxDownscale.drawImage(canvasPreview, 0, 0, canvasDownscale.width, canvasDownscale.height);
    else
        resample_single(canvasDownscale, canvasPreview);
}
//More accurate canvas resizing
//  From: https://stackoverflow.com/questions/18922880/html5-canvas-resize-downscale-image-high-quality @ViliusL
//  Note: Modified from original posting to better fit my specific needs
/**
 * Hermite resize - fast image resize/resample using Hermite filter. 1 cpu version!
 */
 function resample_single(targetcanvas, sourcecanvas) {
    var width_source = Math.round(sourcecanvas.width);
    var height_source = Math.round(sourcecanvas.height);
    width = Math.round(targetcanvas.width);
    height = Math.round(targetcanvas.height);

    var ratio_w = width_source / width;
    var ratio_h = height_source / height;
    var ratio_w_half = Math.ceil(ratio_w / 2);
    var ratio_h_half = Math.ceil(ratio_h / 2);

    var ctxsource = sourcecanvas.getContext("2d");
    var imgsource = ctxsource.getImageData(0, 0, width_source, height_source);
    var img2 = ctxsource.createImageData(width, height);
    var datasource = imgsource.data;
    var data2 = img2.data;

    for (var j = 0; j < height; j++) {
        for (var i = 0; i < width; i++) {
            var x2 = (i + j * width) * 4;
            var weight = 0;
            var weights = 0;
            var weights_alpha = 0;
            var gx_r = 0;
            var gx_g = 0;
            var gx_b = 0;
            var gx_a = 0;
            var center_y = (j + 0.5) * ratio_h;
            var yy_start = Math.floor(j * ratio_h);
            var yy_stop = Math.ceil((j + 1) * ratio_h);
            for (var yy = yy_start; yy < yy_stop; yy++) {
                var dy = Math.abs(center_y - (yy + 0.5)) / ratio_h_half;
                var center_x = (i + 0.5) * ratio_w;
                var w0 = dy * dy; //pre-calc part of w
                var xx_start = Math.floor(i * ratio_w);
                var xx_stop = Math.ceil((i + 1) * ratio_w);
                for (var xx = xx_start; xx < xx_stop; xx++) {
                    var dx = Math.abs(center_x - (xx + 0.5)) / ratio_w_half;
                    var w = Math.sqrt(w0 + dx * dx);
                    if (w >= 1) {
                        //pixel too far
                        continue;
                    }
                    //hermite filter
                    weight = 2 * w * w * w - 3 * w * w + 1;
                    var pos_x = 4 * (xx + yy * width_source);
                    //alpha
                    gx_a += weight * datasource[pos_x + 3];
                    weights_alpha += weight;
                    //colors
                    if (datasource[pos_x + 3] < 255)
                        weight = weight * datasource[pos_x + 3] / 250;
                    gx_r += weight * datasource[pos_x];
                    gx_g += weight * datasource[pos_x + 1];
                    gx_b += weight * datasource[pos_x + 2];
                    weights += weight;
                }
            }
            data2[x2] = gx_r / weights;
            data2[x2 + 1] = gx_g / weights;
            data2[x2 + 2] = gx_b / weights;
            data2[x2 + 3] = gx_a / weights_alpha;
        }
    }

    //draw
    let ctxtarget = targetcanvas.getContext("2d");
    ctxtarget.clearRect(0, 0, width_source, height_source);
    ctxtarget.putImageData(img2, 0, 0);
}


//Fast n choose k calculation
// From: https://stackoverflow.com/questions/37679987/efficient-computation-of-n-choose-k-in-node-js @le_m
// Notes: n and k must be within the range 0 -> 1000 (they definitely will for this project)
function nchoosek(n, k) {
    return Math.exp(logf[n] - logf[n-k] - logf[k]);
}
//Code used to precompute logf array
// var size = 1000, logf = new Array(size);
// logf[0] = 0;
// for (var i = 1; i <= size; ++i) logf[i] = logf[i-1] + Math.log(i);

function computeEveryStringInfluenceMap(doAutoDownload) {
    COMPUTE_bufferLength = canvasDownscale.width * canvasDownscale.height;
    let completedIDArray = new Set();
    console.log("Beginning computing string influence map!");

    //Loop through parent nails
    let chunkNum = 0;
    for (let parentNailIndex = 0;parentNailIndex < NailInfoObject.nailLocArr.length;parentNailIndex++) {
        console.log("parentNailIndex: " + parentNailIndex)
        COMPUTE_parentNailLoc = NailInfoObject.nailLocArr[parentNailIndex];
        //Loop through child nails
        for (let childNailIndex = 0;childNailIndex < NailInfoObject.nailLocArr.length;childNailIndex++) {
            COMPUTE_ID = parentNailIndex + "," + childNailIndex;
            if (parentNailIndex == childNailIndex || completedIDArray.has(COMPUTE_ID))
                continue;

            COMPUTE_childNailLoc = NailInfoObject.nailLocArr[childNailIndex];
            //Loop through pixels on canvas and get distance from line connecting parent/child nails
            COMPUTE_index = 0;
            // COMPUTE_distance = 0;
            // for (let px = 0;px < canvasDownscale.width;px++) {
            //     for (let py = 0;py < canvasDownscale.height;py++,index++) {
            for (let py = 0;py < canvasDownscale.height;py++) {
                for (let px = 0;px < canvasDownscale.width;px++,COMPUTE_index++) {
                    COMPUTE_distance = distancePointToLine(px, py, COMPUTE_parentNailLoc.x, COMPUTE_parentNailLoc.y, COMPUTE_childNailLoc.x, COMPUTE_childNailLoc.y);
                    // if (parentNailIndex == 1 && childNailIndex == 3)
                        // console.log(px + "," + py + "  " + COMPUTE_index + "," + chunkNum + "  " + COMPUTE_distance);
                    COMPUTE_influenceArr[(COMPUTE_index + COMPUTE_bufferLength*chunkNum)] = distanceToInfluence(COMPUTE_distance);
                }
            }
            NailInfoObject.nailStringsPixelComputedInfluenceMap[COMPUTE_ID] = COMPUTE_influenceArr.subarray(chunkNum*COMPUTE_bufferLength, (chunkNum+1)*COMPUTE_bufferLength);
            completedIDArray.add(COMPUTE_ID);
            completedIDArray.add(childNailIndex + "," + parentNailIndex);
            chunkNum++
        }
    }
    console.log("Finished computing string influence map!");
    downloadinfluencemapbutton.disabled = false;
}
function debugInfluence(targetIndex, doUpdateScore) {
    let ogTargetIndex = targetIndex;
    //Convert if written in nail notation
    if (targetIndex.includes(",")) {
        targetIndex = Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap).indexOf(targetIndex);
        //Flip if still not found
        if (targetIndex == -1) {
            let commaIndex = ogTargetIndex.indexOf(",");
            let num1 = ogTargetIndex.substring(0,commaIndex);
            let num2 = ogTargetIndex.substring(commaIndex+1,ogTargetIndex.length);
            targetIndex = Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap).indexOf(num2 + "," + num1);
        }
        //End if still not found
        if (targetIndex == -1) {
            console.log("Nail " + ogTargetIndex + " is not in database.");
            return;
        }
    }

    function indexToPixel(index) {
        let y = Math.floor(index / canvasCompute.width);
        let x = index % canvasCompute.width;
        return {
            x: x,
            y: y
        }
    }

    ctxCompute.globalCompositeOperation = "source-over";
    ctxCompute.fillStyle = "black";
    ctxCompute.fillRect(0,0,canvasCompute.width,canvasCompute.height);
    ctxCompute.globalCompositeOperation = "lighter";
    let loc;
    let grayscaleValueArr = Object.values(NailInfoObject.nailStringsPixelComputedInfluenceMap)[targetIndex];
    for (let i = 0;i < grayscaleValueArr.length;i++) {
        let grayscaleValue = grayscaleValueArr[i]*FullStrengthPixelInfluencePerStringMult;
        ctxCompute.fillStyle = "rgb(" + grayscaleValue + "," + grayscaleValue + "," + grayscaleValue + ")"; //We use "lighter" as the blend mode so we must add white where strings go and inverse in the end
        loc = indexToPixel(i);
        ctxCompute.fillRect(loc.x, loc.y, 1, 1);
    }

    //Set Score Text
    primeQuickComputeStringCache();
    if (doUpdateScore)
        debugscore.value = computeScoreOfAddingProposedString(Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap)[targetIndex], targetIndex);
}

function loadImagePrimer() {
    let elem = document.getElementById('imgchoice');
    //On upload file
    elem.addEventListener("change", () => {
        if (!FileReader || !elem.files || !elem.files.length)
            return;
        let fr = new FileReader();
        //On filereader done rendering blob
        fr.onload = function () {
            let blob = fr.result;
            let image = new Image();
            //On image done parsing blob
            image.onload = function() {
                //Draw blob image to canvas
                canvasPreview.width = image.width;
                canvasPreview.height = image.height;
                ctxPreview.drawImage(image, 0, 0);
                //Grayscale Canvas
                const imageData = ctxPreview.getImageData(0, 0, canvasPreview.width, canvasPreview.height);
                const data = imageData.data;
                for (let i = 0; i < data.length; i += 4) {
                    const red = data[i];
                    const green = data[i + 1];
                    const blue = data[i + 2];
                    const grayscale = (red + green + blue) / 3;
                    data[i] = data[i + 1] = data[i + 2] = grayscale;
                }
                ctxPreview.putImageData(imageData, 0, 0);

                //Vectorize Image
                // Here I use a local array and then transfer it to the global array because for some
                // reason when I used the global array here, it lagged my firefox to the point this
                // operation which takes fractions of a second now took well over 10 minutes. I don't
                // know why. Literally absolutely no clue. My best direction of a guess is because
                // I can use "const" instead of "let" in this local scope.
                const temp = ctxPreview.getImageData(0, 0, canvasPreview.width, canvasPreview.height).data;
                const pixelCount = temp.length / 4;
                const PixelGrayscaleDataArr_local = new Uint8Array(pixelCount);
                for (let i = 0, j = 0; i < temp.length; i += 4, j++) {
                    PixelGrayscaleDataArr_local[j] = temp[i];
                }
                PixelGrayscaleDataArr = PixelGrayscaleDataArr_local;
            }

            // Set the source of the Image object to the blob URL
            image.src = blob;
        }
        fr.readAsDataURL(elem.files[0]);
    });
}
loadImagePrimer();

function firstNonzeroIndex(arr) {
    return arr.findIndex(function (val) { //We use this index to know if the array has been initialized or not
        return val > 0;
    });
}
function primeQuickComputeStringCache() {
    let index = 0;
    let ctxComputeData = ctxCompute.getImageData(0,0,canvasCompute.width,canvasCompute.height).data;
    let ctxDownscaleData = ctxDownscale.getImageData(0,0,canvasDownscale.width,canvasDownscale.height).data;
    let firstNonzeroTargetIndex = firstNonzeroIndex(quickTargetGrayscaleCache);
    for (let y = 0;y < canvasCompute.height;y++) {
        for (let x = 0;x < canvasCompute.width;x++,index++) {
            quickCurGrayscaleCache[index] = ctxComputeData[index*4];
            if (firstNonzeroTargetIndex == -1) {
                if (ctxDownscaleData[index*4] != 0) {
                    quickTargetGrayscaleCache[index] = ctxDownscaleData[index*4];
                }
            }
        }
    }

    //V1
    // let index = 0;
    // for (let y = 0;y < canvasCompute.height;y++) {
    //     for (let x = 0;x < canvasCompute.width;x++,index++) {
    //         quickCurGrayscaleCache[index] = ctxCompute.getImageData(x,y,1,1).data[0];
    //         quickTargetGrayscaleCache[index] = ctxDownscale.getImageData(x,y,1,1).data[0];
    //     }
    // }
}
function addStringToComputed(influenceMapIndex) {
    let influenceMap = Object.values(NailInfoObject.nailStringsPixelComputedInfluenceMap)[influenceMapIndex];
    //Draw/add influence map
    let index = 0;
    let curValue, deltaValue;
    for (let y = 0;y < canvasCompute.height;y++) {
        for (let x = 0;x < canvasCompute.width;x++,index++) {
            curValue = quickCurGrayscaleCache[index];
            deltaValue = influenceMap[index]*FullStrengthPixelInfluencePerStringMult;
            let clampedGrayscale = Math.min(curValue+deltaValue, 255);
            ctxCompute.fillStyle = "rgb(" + clampedGrayscale + "," + clampedGrayscale + "," + clampedGrayscale + ")";
            ctxCompute.fillRect(x,y,1,1);
        }
    }
    //Remove from database so isn't repeatedly checked (we don't want the same string checked twice since it adds nothing)
    let ID = Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap)[influenceMapIndex];
    calculatedStringSet.add(ID);
    // delete NailInfoObject.nailStringsPixelComputedInfluenceMap[ID];
}
function computeScoreOfAddingProposedString(ID, index) {
    //Output is [positiveInfluence, negativeInfluence]
    // positiveInfluence is positive contributions upon drawing
    // negativeInfluence is negative contributions uppon drawing

    //Algorithm thoughts.
    /*
    * We can't treat "needs to be drawn more" pixels as positive and "overdrawn" pixels as negative or else the algorithm
    * doesn't care about recreating the picture and cares more about "seeking a score of 0" by balancing random negative
    * and positive scores unrelated to the target image.
    * 
    * We can't minimize overdrawing or else the algorithm just keeps drawing lines side by side in touching nails because
    * the shortest line has the least overdraw
    * 
    * We can maximize filling in the most good per pass ignoring the overdraw
    */

    if (index == -1 || calculatedStringSet.has(ID))
        return [-999999999, -999999999]; //Has to be more influential than a realistic score, but not so large it overflows when added
    else {
        let NEGATIVEWEIGHT = .0001;
        let NEGATIVESUBTRACT = .0004;
        let positiveScore = 0;
        let targetValue, curValue, deltaValue, addTemp, positiveInfluence;
        let index2 = 0;
        const values = Object.values(NailInfoObject.nailStringsPixelComputedInfluenceMap);
        const valueArr = values[index];
        for (let i = 0;i < valueArr.length;i++) {
            deltaValue = valueArr[i]*FullStrengthPixelInfluencePerStringMult;
            if (deltaValue == 0)
                continue;
            
            targetValue = quickTargetGrayscaleCache[i];
            curValue = quickCurGrayscaleCache[i];
            if (targetValue == 0) { //If we are attempting to color a pitch black pixel
                positiveScore -= NEGATIVESUBTRACT; //This should be deltaValue * NEGATIVEWEIGHT but because deltaValue is != 0, we can assume it's 1
                continue;
            } else if (curValue > targetValue) { //If we are already overdrawn, subtract the entire delta from the score (weighted)
                positiveScore -= NEGATIVESUBTRACT; //This should be deltaValue * NEGATIVEWEIGHT but because deltaValue is != 0, we can assume it's 1
                continue;
            }

            // Positive Influence explanation
            /*
            * If curValue + influence < or = targetValue, then positive influence is complete influence
            *
            * If curValue + influence > targetValue, then positive influence is only segment of influence which contributes to meeting targetValue
            *  (even though the total addition supercedes, we just ignore the amount of deltaValue aka influence which passes the target)
            */
            addTemp = curValue + deltaValue;
            if (addTemp <= targetValue) { //If we are so underdrawn that adding the entire delta doesn't matter
                positiveInfluence = deltaValue;
                // console.log(":: " + addTemp + "<=" + targetValue + "," + deltaValue + "," + positiveInfluence);
            } else if (addTemp > targetValue) { //If we are crossing the threshold from underdrawn -> overdrawn
                let underdrawnChange = targetValue - curValue;
                let overdrawnChange = addTemp - targetValue;
                positiveInfluence = underdrawnChange - overdrawnChange*NEGATIVEWEIGHT;
                // console.log(":: " + addTemp + ">" + targetValue + "," + deltaValue + "," + positiveInfluence);
            }
            positiveScore += positiveInfluence;
        }
        // for (let y = 0;y < canvasCompute.height;y++) {
        //     for (let x = 0;x < canvasCompute.width;x++,index2++) {
        //         targetValue = quickTargetGrayscaleCache[index2];
        //         curValue = quickCurGrayscaleCache[index2];
        //         deltaValue = valueArr[index2];
        //         //V3
        //         //Keeping track of the negative influence of a drawing makes the algorithm favor
        //         // drawing nails side by side when the negative outweighs the positive of any line
        //         let difference = targetValue - (curValue + deltaValue);
        //             score += (difference > 0) ? difference : 0;
        //         //V2
        //         //I wanted + score to represent underdrawing and - score to represent overdrawing,
        //         // But that ended up compelling the algorithm to intentionally make negative mistakes
        //         // (drawing in the wrong spot) to cancel out positive mistakes (not having drawn in
        //         // the right spot).
        //         // score += Math.abs(targetValue - (curValue + deltaValue));
        //         //V1
        //         // score += targetValue - (curValue + deltaValue);
        //         // score += quickTargetGrayscaleCache[index2] - (quickCurGrayscaleCache[index2] + values[index][index2]);
        //     }
        // }
        // console.log(ID + "  " + index + "  " + positiveScore);
        return [positiveScore, 0];
    }
}
function computeNextBestString() {
    if (isDoneAddingStrings)
        return;

    if (NailInfoObject.nailNumArr.length == 0) { //Is first string being added (so check ALL combos, and note both parent and child nail)
        resetStringAnalysis();
        //Cache grayscale for quicker computes
        primeQuickComputeStringCache();
        // Loop through all possible combos for the most beneficial starting location.
        let keys = Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap);
        let maximum = {
            //We keep track of negative and positive for a reason
            // Negative implies we have OVERDRAWN
            // Positive implies we have UNDERDRAWN
            // There may be some logic I want to incorporate regarding weighing underdrawing vs overdrawing beyond straightforward distance from 0
            negativescore: 0,
            positivescore: 0,
            negativeID: null,
            positiveID: null,
            perfectID: null
        };
        for (let i = 0;i < keys.length;i++) {
            if (i % 1000 == 0)
                console.log("i: " + i + "/" + (keys.length-1));
            let ID = keys[i];
            let testPositiveScore = computeScoreOfAddingProposedString(ID, i)[0];
            if(testPositiveScore > maximum.positivescore) {
                maximum.positivescore = testPositiveScore;
                maximum.positiveID = ID;
            }
        }
        //Apply most beneficial to canvas and add to nail traversal database
        console.log(maximum);
        if (maximum.perfectID != null) {
            let ID = maximum.perfectID;
            let index = keys.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            NailInfoObject.nailNumArr.push(parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length)));
            console.log("Best String is ID: " + ID + "   Index: " + index);
        } else if (maximum.negativescore > maximum.positivescore) {
            let ID = maximum.negativeID;
            let index = keys.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            NailInfoObject.nailNumArr.push(parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length)));
            console.log("Best String is -ID: " + ID + "   Index: " + index);
        } else if (maximum.positivescore > maximum.negativescore) {
            let ID = maximum.positiveID;
            let index = keys.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            NailInfoObject.nailNumArr.push(parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length)));
            console.log("Best String is +ID: " + ID + "   Index: " + index);
        } else {
            isDoneAddingStrings = true;
        }
    } else { // Is not the very first string (so only check from parent, only note child nail)
        //Cache grayscale for quicker computes
        primeQuickComputeStringCache();
        // Loop through all leftover combinations with the parent being the last used nail
        let allKeysLeft = Object.keys(NailInfoObject.nailStringsPixelComputedInfluenceMap);
        let keys = [], lastUsedNail = NailInfoObject.nailNumArr[NailInfoObject.nailNumArr.length-1];
        for (let ID of allKeysLeft) {
            if (ID.startsWith(lastUsedNail + ",") || ID.endsWith("," + lastUsedNail))
                keys.push(ID);
        }
        let maximum = {
            //We keep track of negative and positive for a reason
            // Negative implies we have OVERDRAWN
            // Positive implies we have UNDERDRAWN
            // There may be some logic I want to incorporate regarding weighing underdrawing vs overdrawing beyond straightforward distance from 0
            negativescore: 0,
            positivescore: 0,
            negativeID: null,
            positiveID: null,
            perfectID: null
        };
        for (let i = 0;i < keys.length;i++) {
            // if (i % 10 == 0)
            //     console.log("i: " + i + "/" + (keys.length-1));
            let ID = keys[i];
            let influenceMapIndex = allKeysLeft.indexOf(ID);
            let testPositiveScore = computeScoreOfAddingProposedString(ID, influenceMapIndex)[0];
            if(testPositiveScore > maximum.positivescore) {
                maximum.positivescore = testPositiveScore;
                maximum.positiveID = ID;
            }
        }
        //Apply most beneficial to canvas and add to nail traversal database
        if (maximum.perfectID != null) {
            let ID = maximum.perfectID;
            let index = allKeysLeft.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            let nailNumsUsed = [parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length))];
            let nailToAdd = nailNumsUsed[0] == lastUsedNail ? nailNumsUsed[1] : nailNumsUsed[0];
            NailInfoObject.nailNumArr.push(nailToAdd);
            console.log("Best String is ID: " + ID + "   Index: " + index);
        } else if (maximum.negativescore > maximum.positivescore) {
            let ID = maximum.negativeID;
            let index = allKeysLeft.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            let nailNumsUsed = [parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length))];
            let nailToAdd = nailNumsUsed[0] == lastUsedNail ? nailNumsUsed[1] : nailNumsUsed[0];
            NailInfoObject.nailNumArr.push(nailToAdd);
            console.log("Best String is -ID: " + ID + "   Index: " + index);
        } else if (maximum.positivescore > maximum.negativescore) {
            let ID = maximum.positiveID;
            let index = allKeysLeft.indexOf(ID);
            addStringToComputed(index);
            let commaIndex = ID.indexOf(",");
            let nailNumsUsed = [parseInt(ID.substring(0,commaIndex)), parseInt(ID.substring(commaIndex+1,ID.length))];
            let nailToAdd = nailNumsUsed[0] == lastUsedNail ? nailNumsUsed[1] : nailNumsUsed[0];
            NailInfoObject.nailNumArr.push(nailToAdd);
            console.log("Best String is +ID: " + ID + "   Index: " + index);
        } else {
            isDoneAddingStrings = true;
        }
    }
}
function resetStringAnalysis() {
    NailInfoObject.nailNumArr = [];
    calculatedStringSet.clear();
    ctxCompute.globalCompositeOperation = "source-over";
    ctxCompute.fillStyle = "black";
    ctxCompute.fillRect(0,0,canvasCompute.width,canvasCompute.height);
    console.log("drawing compute black");
    isDoneAddingStrings = false;
    for (let i = 0;i < quickTargetGrayscaleCache.length;i++) {
        quickTargetGrayscaleCache[i] = 0;
    }
    DISTANCE_downscaleToNailSpace = canvasNail.width/canvasDownscale.width;
    DISTANCE_nailToDownscaleSpace = canvasDownscale.width/canvasNail.width;
}

function drawStrings() {
    // NailInfoObject.nailNumArr = [1,2,1,2,1,2,1,2,1,2,1,0,1,0,1,0,2,0,2];
    let nailNumArr = NailInfoObject.nailNumArr;
    ctxNail.clearRect(0,0,canvasNail.width,canvasNail.height);
    drawNails();
    for (let i = 0;i < nailNumArr.length;i++) {
        let curNailLocObject = NailInfoObject.nailLocArr[nailNumArr[i]];
        // console.log(i + ": " + JSON.stringify(curNailLocObject));
        if (i == 0) {
            ctxNail.strokeStyle = "rgba(0,0,0,1)";
            ctxNail.lineWidth = 1;
            ctxNail.beginPath();
            ctxNail.moveTo(curNailLocObject.x, curNailLocObject.y);
        } else {
            ctxNail.lineTo(curNailLocObject.x, curNailLocObject.y);
            ctxNail.stroke();
        }
    }
}
function downloadJSONObject(obj) {
    const blob = new Blob([JSON.stringify(obj)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    // Example: Trigger download link for the generated JSON file
    const downloadLink = document.createElement('a');
    downloadLink.href = url;
    downloadLink.download = 'output.json';
    // document.body.appendChild(downloadLink);
    downloadLink.click();

    // Clean up the object URL after download
    URL.revokeObjectURL(url);
}